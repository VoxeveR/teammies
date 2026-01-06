package com.voxever.teammies.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.voxever.teammies.dto.quiz.websocket.FinalAnswerCalculationRequestDto;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.dto.quiz.websocket.HighlightSelectionDto;
import com.voxever.teammies.dto.quiz.websocket.PlayerSelectionDto;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;
import com.voxever.teammies.service.QuizSessionService;
import com.voxever.teammies.service.QuizTeamService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class QuizWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final QuizTeamRepository quizTeamRepository;
    private final QuizSessionService quizSessionService;
    private final QuizTeamService quizTeamService;

    private final Map<String, PlayerSessionInfo> playerSessionMap = new HashMap<>();

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   QuizSessionRepository quizSessionRepository,
                                   QuizRepository quizRepository,
                                   QuizPlayerRepository quizPlayerRepository,
                                   QuizTeamRepository quizTeamRepository,
                                   QuizSessionService quizSessionService,
                                   QuizTeamService quizTeamService) {
        this.messagingTemplate = messagingTemplate;
        this.quizSessionRepository = quizSessionRepository;
        this.quizRepository = quizRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.quizTeamRepository = quizTeamRepository;
        this.quizSessionService = quizSessionService;
        this.quizTeamService = quizTeamService;
    }

    // Inner class to store player session info
    private static class PlayerSessionInfo {
        Long playerId;
        String sessionJoinCode;

        PlayerSessionInfo(Long playerId, String sessionJoinCode) {
            this.playerId = playerId;
            this.sessionJoinCode = sessionJoinCode;
        }
    }

    @Transactional
    @MessageMapping("/quiz-session/{sessionJoinCode}/team/{teamCode}/answer")
    public void handleTeamAnswer(
            @DestinationVariable String sessionJoinCode,
            @DestinationVariable String teamCode,
            @Payload HighlightSelectionDto answerPayload,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Player {} selected option: {} for question {}",
                answerPayload.getPlayerId(),
                answerPayload.getSelectedOption(),
                answerPayload.getQuestionId());

        // Validate player exists first
        Optional<QuizPlayer> playerOpt = quizPlayerRepository.findById(answerPayload.getPlayerId());
        if (playerOpt.isEmpty()) {
            log.warn("Player {} not found in database", answerPayload.getPlayerId());
            return;
        }
        
        QuizPlayer player = playerOpt.get();
        
        // Check if player has a team
        if (player.getTeam() == null) {
            log.warn("Player {} attempted to answer but is not in any team (likely disconnected)", 
                    answerPayload.getPlayerId());
            return;
        }
        
        // Use the player's team reference - if player is in a team, they're in the right session
        QuizTeam team = player.getTeam();

        // Store the player's current selection
        player.setCurrentQuestionId(answerPayload.getQuestionId());
        player.setCurrentHighlight(answerPayload.getSelectedOption());
        player.setCurrentHighlightIndex(answerPayload.getSelectedIndex());
        quizPlayerRepository.save(player);

        // Build player selection event to broadcast to all
        PlayerSelectionDto selectionEvent = PlayerSelectionDto.builder()
                .playerId(answerPayload.getPlayerId())
                .playerName(player.getNickname())
                .questionId(answerPayload.getQuestionId())
                .selectedOption(answerPayload.getSelectedOption())
                .selectedIndex(answerPayload.getSelectedIndex())
                .timestamp(answerPayload.getTimestamp())
                .build();

        // Broadcast to all players in this session to show who answered what
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/team/" + teamCode + "/selection",
                selectionEvent
        );
    }

    @MessageMapping("/quiz-session/{sessionJoinCode}/team/{teamCode}/calculate-final-answer")
    public void calculateAndBroadcastFinalAnswer(
            @DestinationVariable String sessionJoinCode,
            @DestinationVariable String teamCode,
            @Payload FinalAnswerCalculationRequestDto request) {

        log.info("Calculating final answer for team: {}, question: {}",
                request.getTeamId(),
                request.getQuestionId());

        FinalTeamAnswerDto finalAnswer = quizSessionService.calculateAndSaveFinalTeamAnswer(
                request.getTeamId(),
                request.getQuestionId()
        );

        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/team/" + teamCode + "/final-answer",
                finalAnswer
        );
    }

    @MessageMapping("/quiz-session/{sessionJoinCode}/team/{teamCode}/player-disconnect")
    public void handlePlayerDisconnect(
            @DestinationVariable String sessionJoinCode,
            @DestinationVariable String teamCode,
            @Payload Map<String, Long> payload) {

        Long playerId = payload.get("playerId");
        
        log.info("=== PLAYER DISCONNECT MESSAGE RECEIVED ===");
        log.info("Player ID: {}", playerId);
        log.info("Session: {}, Team: {}", sessionJoinCode, teamCode);

        try {
            QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

            // Team might have been already deleted if it was the last player
            QuizTeam team = quizTeamRepository.findByJoinCodeAndQuizSession(teamCode, session)
                    .orElse(null);
            
            // If team is gone, it means it was already deleted (last player left)
            if (team == null) {
                log.info("Team {} already deleted for session {}, skipping disconnect handling", teamCode, sessionJoinCode);
                return;
            }

            // Refresh team to ensure players collection is loaded
            team = quizTeamRepository.findById(team.getId())
                    .orElse(null);
            
            if (team == null) {
                log.info("Team no longer exists in database, skipping disconnect handling");
                return;
            }

            QuizPlayer player = quizPlayerRepository.findById(playerId)
                    .orElse(null);
            
            if (player == null) {
                log.info("Player {} already deleted from database, skipping disconnect handling", playerId);
                return;
            }

            log.info("Found player: {}, team: {}, session: {}", player.getNickname(), team.getName(), session.getJoinCode());

            // Add small delay to ensure message is delivered before processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.info("Calling handlePlayerLeft...");
            quizTeamService.handlePlayerLeft(playerId, team, sessionJoinCode);
            log.info("handlePlayerLeft completed");
        } catch (Exception e) {
            log.error("Error processing player disconnect", e);
            throw e;
        }
    }
}