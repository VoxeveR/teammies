package com.voxever.teammies.service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

import jakarta.transaction.Transactional;

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
        QuizPlayer savedPlayer = quizPlayerRepository.save(player);

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
}