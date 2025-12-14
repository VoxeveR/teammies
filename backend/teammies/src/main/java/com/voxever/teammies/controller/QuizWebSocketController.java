package com.voxever.teammies.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.voxever.teammies.dto.quiz.websocket.HighlightSelectionDto;
import com.voxever.teammies.dto.quiz.websocket.PlayerSelectionDto;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.dto.quiz.websocket.FinalAnswerCalculationRequest;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;
import com.voxever.teammies.service.QuizSessionService;

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

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   QuizSessionRepository quizSessionRepository,
                                   QuizRepository quizRepository,
                                   QuizPlayerRepository quizPlayerRepository,
                                   QuizTeamRepository quizTeamRepository,
                                   QuizSessionService quizSessionService) {
        this.messagingTemplate = messagingTemplate;
        this.quizSessionRepository = quizSessionRepository;
        this.quizRepository = quizRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.quizTeamRepository = quizTeamRepository;
        this.quizSessionService = quizSessionService;
    }

    @MessageMapping("/quiz-session/{sessionJoinCode}/team/{teamCode}/answer")
    public void handleTeamAnswer(
            @DestinationVariable String sessionJoinCode,
            @DestinationVariable String teamCode,
            @Payload HighlightSelectionDto answerPayload) {

        log.info("Player {} selected option: {} for question {}",
                answerPayload.getPlayerId(),
                answerPayload.getSelectedOption(),
                answerPayload.getQuestionId());

        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        QuizTeam team = quizTeamRepository.findByJoinCode(teamCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found"));

        // Validate player exists
        QuizPlayer player = quizPlayerRepository.findById(answerPayload.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player not found"));

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
            @Payload FinalAnswerCalculationRequest request) {

        log.info("Calculating final answer for team: {}, question: {}",
                request.getTeamId(),
                request.getQuestionId());

        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        // Calculate final team answer and persist to database
        FinalTeamAnswerDto finalAnswer = quizSessionService.calculateAndSaveFinalTeamAnswer(
                request.getTeamId(),
                request.getQuestionId()
        );

        // Broadcast final answer to all players in the team
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/team/" + teamCode + "/final-answer",
                finalAnswer
        );
    }
}