package com.voxever.teammies.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.voxever.teammies.dto.team.CreateTeamRequest;
import com.voxever.teammies.dto.team.CreateTeamResponse;
import com.voxever.teammies.dto.team.JoinTeamRequest;
import com.voxever.teammies.dto.team.JoinTeamResponse;
import com.voxever.teammies.dto.team.TeamMembersDto;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.entity.Team;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;
import com.voxever.teammies.repository.TeamRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuizTeamService {

    private final QuizTeamRepository quizTeamRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final TeamRepository teamRepository;
    private final QuizSessionWebSocketBroadcasts webSocketService;
    private final Random random = new Random();

    public QuizTeamService(QuizTeamRepository quizTeamRepository,
                          QuizSessionRepository quizSessionRepository,
                          QuizPlayerRepository quizPlayerRepository,
                          TeamRepository teamRepository,
                          QuizSessionWebSocketBroadcasts webSocketService) {
        this.quizTeamRepository = quizTeamRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.teamRepository = teamRepository;
        this.webSocketService = webSocketService;
    }

    @Transactional
    public ResponseEntity<CreateTeamResponse> createTeam(String sessionJoinCode, CreateTeamRequest request) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create team in this session");
        }

        // Check if team name is already taken in this session
        if (quizTeamRepository.findByNameAndQuizSessionId(request.getTeamName(), session.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team name already taken");
        }

        // Fetch the existing QuizPlayer
        QuizPlayer player = quizPlayerRepository.findById(request.getQuizPlayerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player not found"));

        League league = session.getQuiz().getLeague();

        // Check if persistent Team already exists in the league
        var persistentTeamOpt = teamRepository.findByLeagueAndName(league, request.getTeamName());
        
        String teamJoinCode;
        if (persistentTeamOpt.isPresent()) {
            // Reuse the join code from existing persistent team
            teamJoinCode = persistentTeamOpt.get().getJoinCode();
        } else {
            // Generate unique join code for new persistent team
            teamJoinCode = generateUniqueTeamJoinCode();
            // Create persistent Team with this code
            Team persistentTeam = Team.builder()
                    .name(request.getTeamName())
                    .joinCode(teamJoinCode)
                    .league(league)
                    .build();
            teamRepository.save(persistentTeam);
        }

        // Create QuizTeam with the same join code from persistent team
        QuizTeam team = QuizTeam.builder()
                .name(request.getTeamName())
                .joinCode(teamJoinCode)
                .quizSession(session)
                .players(new HashSet<>())
                .build();

        QuizTeam savedTeam = quizTeamRepository.save(team);

        // Assign player to team and mark as captain
        player.setTeam(savedTeam);
        player.setCaptain(true);
        quizPlayerRepository.save(player);

        savedTeam.getPlayers().add(player);

        webSocketService.broadcastTeamCreated(sessionJoinCode, savedTeam);
        webSocketService.broadcastPlayerJoined(sessionJoinCode, savedTeam, player);

        return ResponseEntity.ok(CreateTeamResponse.builder()
                .teamId(savedTeam.getId())
                .teamName(savedTeam.getName())
                .teamJoinCode(savedTeam.getJoinCode())
                .quizSessionId(session.getId())
                .playerUsername(player.getNickname())
                .isCaptain(true)
                .message("Team created successfully. You are the team captain.")
                .build());
    }

    @Transactional
    public ResponseEntity<JoinTeamResponse> joinTeam(String sessionJoinCode, JoinTeamRequest request) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot join team in this session");
        }

        QuizPlayer player = quizPlayerRepository.findById(request.getQuizPlayerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player not found"));

        League league = session.getQuiz().getLeague();
        
        // Try to find team in current session using both join code and session
        var quizTeamOpt = quizTeamRepository.findByJoinCodeAndQuizSession(request.getTeamJoinCode(), session);
        
        QuizTeam team;
        boolean isNewTeamForSession = false;
        
        if (quizTeamOpt.isPresent()) {
            // Team already exists in current session
            team = quizTeamOpt.get();
        } else {
            // Team doesn't exist in this session - check if it exists in another session (cross-session join)
            List<QuizTeam> existingTeams = quizTeamRepository.findAllByJoinCode(request.getTeamJoinCode());
            
            if (!existingTeams.isEmpty()) {
                // Team exists in another session - find the persistent team and create new session team
                QuizTeam existingTeam = existingTeams.get(0);  // Get first match
                var persistentTeamOpt = teamRepository.findByLeagueAndName(league, existingTeam.getName());
                
                if (persistentTeamOpt.isEmpty()) {
                    throw new ResponseStatusException(NOT_FOUND, "Team not found in league");
                }
                
                // Create new QuizTeam for current session using persistent team's join code
                Team persistentTeam = persistentTeamOpt.get();
                QuizTeam newSessionTeam = QuizTeam.builder()
                        .name(persistentTeam.getName())
                        .joinCode(persistentTeam.getJoinCode())
                        .quizSession(session)
                        .players(new HashSet<>())
                        .build();
                team = quizTeamRepository.save(newSessionTeam);
                isNewTeamForSession = true;  // Mark that this is a new team for this session
            } else {
                // No team with this code found anywhere
                throw new ResponseStatusException(NOT_FOUND, "Invalid team join code");
            }
        }
        
        // Ensure persistent Team exists in the league (fallback)
        var persistentTeamOpt = teamRepository.findByLeagueAndName(league, team.getName());
        
        if (persistentTeamOpt.isEmpty()) {
            // Create persistent Team if it doesn't exist (fallback)
            Team persistentTeam = Team.builder()
                    .name(team.getName())
                    .joinCode(team.getJoinCode())
                    .league(league)
                    .build();
            teamRepository.save(persistentTeam);
        }

        player.setTeam(team);
        team.getPlayers().add(player);  // Add player to team's players collection!
        QuizPlayer savedPlayer = quizPlayerRepository.save(player);
        quizPlayerRepository.flush();  // Flush player to ensure team reference is persisted
        
        quizTeamRepository.save(team);  // Save team to persist the collection update
        quizTeamRepository.flush();  // Flush team to ensure players collection is persisted
        
        // Ensure the team reference is properly persisted before broadcasting
        savedPlayer = quizPlayerRepository.findById(savedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload player after save"));

        // Broadcast team created event if this is a new team for this session
        if (isNewTeamForSession) {
            webSocketService.broadcastTeamCreated(sessionJoinCode, team);
        }

        // Always broadcast player joined event
        webSocketService.broadcastPlayerJoined(sessionJoinCode, team, savedPlayer);

        return ResponseEntity.ok(JoinTeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .teamJoinCode(team.getJoinCode())
                .quizSessionId(session.getId())
                .playerUsername(savedPlayer.getNickname())
                .isCaptain(false)
                .message("Successfully joined team: " + team.getName())
                .build());
    }

    private String generateUniqueTeamJoinCode() {
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (!quizTeamRepository.findAllByJoinCode(code).isEmpty());
        return code;
    }

    @Transactional
    public ResponseEntity<TeamMembersDto> getTeamMembers(String sessionJoinCode, String teamJoinCode) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        QuizTeam team = quizTeamRepository.findByJoinCodeAndQuizSession(teamJoinCode, session)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found in this session"));

        java.util.List<TeamMembersDto.TeamMemberInfoDto> memberInfos = team.getPlayers().stream()
                .map(player -> TeamMembersDto.TeamMemberInfoDto.builder()
                        .playerId(player.getId())
                        .nickname(player.getNickname())
                        .isCaptain(player.isCaptain())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(TeamMembersDto.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .members(memberInfos)
                .build());
    }

    public void handlePlayerLeft(Long playerId, QuizTeam team, String sessionJoinCode) {
        log.info("=== HANDLING PLAYER LEFT ===");
        log.info("Player ID: {}, Team: {}, Session: {}", playerId, team.getName(), sessionJoinCode);
        
        QuizPlayer player = null;
        QuizPlayer newCaptain = null;
        boolean teamDeleted = false;
        boolean shouldBroadcast = false;

        // Wrap all DB operations in their own transaction to handle concurrent modifications
        try {
            player = quizPlayerRepository.findById(playerId).orElse(null);
            
            if (player == null) {
                log.warn("Player {} not found", playerId);
                return;
            }

            log.info("Found player: {}", player.getNickname());

            // Refresh team from database to get latest players collection
            team = quizTeamRepository.findByIdWithPlayers(team.getId()).orElse(null);
            
            if (team == null) {
                log.warn("Team not found for player: {}", playerId);
                return;
            }

            // Check if player is still in this team - might have been removed by concurrent request
            // Use ID comparison since Hibernate may return different object instances
            boolean playerInTeam = team.getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(playerId));
            
            if (!playerInTeam) {
                log.info("Player {} already removed from team by another request, will broadcast", playerId);
                shouldBroadcast = true; // Still broadcast even though already removed
                
                // Refresh team from database to get the latest state
                Optional<QuizTeam> refreshedTeamOpt = quizTeamRepository.findByIdWithPlayers(team.getId());
                
                if (refreshedTeamOpt.isPresent()) {
                    team = refreshedTeamOpt.get();
                    log.info("Refreshed team has {} players", team.getPlayers().size());
                    
                    // Check if team is now empty (all players left)
                    if (team.getPlayers().isEmpty()) {
                        log.info("Team is now empty after refresh, deleting team");
                        performDatabaseDelete(team);
                        teamDeleted = true;
                    } else {
                        // Check if team still needs a captain
                        boolean hasCaptain = team.getPlayers().stream().anyMatch(QuizPlayer::isCaptain);
                        log.info("Team has captain: {}", hasCaptain);
                        
                        if (!hasCaptain) {
                            log.info("Team has no captain, assigning one");
                            QuizPlayer randomPlayer = team.getPlayers().stream()
                                    .findFirst()
                                    .orElse(null);
                            
                            if (randomPlayer != null) {
                                randomPlayer.setCaptain(true);
                                performDatabaseSave(randomPlayer);
                                newCaptain = randomPlayer;
                                log.info("New captain assigned: {}", randomPlayer.getNickname());
                            }
                        }
                    }
                } else {
                    // Team was deleted (became empty)
                    log.info("Team was deleted by another request");
                    teamDeleted = true;
                }
            } else {
                // Find the player instance in the team's collection (may be different object due to Hibernate)
                QuizPlayer playerInCollection = team.getPlayers().stream()
                        .filter(p -> p.getId().equals(playerId))
                        .findFirst()
                        .orElse(null);
                
                if (playerInCollection != null) {
                    boolean wasCaptain = playerInCollection.isCaptain();
                    
                    // Remove player from team using the correct instance
                    team.getPlayers().remove(playerInCollection);
                    
                    log.info("Player removed from team. Team now has {} members", team.getPlayers().size());

                    // Check if team is now empty
                    if (team.getPlayers().isEmpty()) {
                        // Delete the team (cascade will handle player deletion)
                        log.info("Team is empty, deleting team: {}", team.getName());
                        performDatabaseDelete(team);
                        teamDeleted = true;
                    } else {
                        // Delete the player who left (not just set team to null)
                        performDatabaseDeletePlayer(playerInCollection);
                        
                        // If the left player was the captain, assign a new captain
                        if (wasCaptain) {
                            log.info("Left player was captain, assigning new captain");
                            QuizPlayer randomPlayer = team.getPlayers().stream()
                                    .findFirst()
                                    .orElse(null);
                            
                            if (randomPlayer != null) {
                                randomPlayer.setCaptain(true);
                                performDatabaseSave(randomPlayer);
                                newCaptain = randomPlayer;
                                log.info("New captain assigned: {}", randomPlayer.getNickname());
                            }
                        }
                        
                        // Save team changes
                        performDatabaseSave(team);
                    }
                }
                
                shouldBroadcast = true;
            }
            
        } catch (Exception e) {
            log.error("Error during player disconnect handling: {}", e.getMessage(), e);
            shouldBroadcast = true; // Try to broadcast even on error
        }

        // Broadcast the event - always try to broadcast when we have player and team
        if (shouldBroadcast && player != null && team != null) {
            try {
                log.info("Broadcasting player left event");
                webSocketService.broadcastPlayerLeft(sessionJoinCode, team, player, newCaptain, teamDeleted);
            } catch (Exception e) {
                log.error("Error broadcasting player left: {}", e.getMessage(), e);
                // Broadcast failure shouldn't crash the system
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void performDatabaseSave(Object entity) {
        if (entity instanceof QuizPlayer) {
            quizPlayerRepository.save((QuizPlayer) entity);
            quizPlayerRepository.flush();
        } else if (entity instanceof QuizTeam) {
            quizTeamRepository.save((QuizTeam) entity);
            quizTeamRepository.flush();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void performDatabaseDeletePlayer(QuizPlayer player) {
        quizPlayerRepository.delete(player);
        quizPlayerRepository.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void performDatabaseDelete(QuizTeam team) {
        quizTeamRepository.delete(team);
        quizTeamRepository.flush();
    }
}
