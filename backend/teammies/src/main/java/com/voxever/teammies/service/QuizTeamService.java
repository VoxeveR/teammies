package com.voxever.teammies.service;

import java.util.HashSet;
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
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;

import jakarta.transaction.Transactional;

@Service
public class QuizTeamService {

    private final QuizTeamRepository quizTeamRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final QuizSessionWebSocketBroadcasts webSocketService;
    private final Random random = new Random();

    public QuizTeamService(QuizTeamRepository quizTeamRepository,
                          QuizSessionRepository quizSessionRepository,
                          QuizPlayerRepository quizPlayerRepository,
                          QuizSessionWebSocketBroadcasts webSocketService) {
        this.quizTeamRepository = quizTeamRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.quizPlayerRepository = quizPlayerRepository;
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

        // Generate unique team join code
        String teamJoinCode = generateUniqueTeamJoinCode();

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

        QuizTeam team = quizTeamRepository.findByJoinCode(request.getTeamJoinCode())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Invalid team join code"));

        if (!team.getQuizSession().getId().equals(session.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "Team does not belong to this quiz session");
        }
        
        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot join team in this session");
        }

        QuizPlayer player = quizPlayerRepository.findById(request.getQuizPlayerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player not found"));

        player.setTeam(team);
        QuizPlayer savedPlayer = quizPlayerRepository.save(player);

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
        } while (quizTeamRepository.findByJoinCode(code).isPresent());
        return code;
    }

    @Transactional
    public ResponseEntity<TeamMembersDto> getTeamMembers(String sessionJoinCode, String teamJoinCode) {
        QuizTeam team = quizTeamRepository.findByJoinCode(teamJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found"));

        QuizSession session = team.getQuizSession();
        if (session == null || !session.getJoinCode().equals(sessionJoinCode)) {
            throw new ResponseStatusException(NOT_FOUND, "Team does not belong to this session");
        }

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