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
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class QuizWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final QuizTeamRepository quizTeamRepository;

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   QuizSessionRepository quizSessionRepository,
                                   QuizRepository quizRepository,
                                   QuizPlayerRepository quizPlayerRepository,
                                   QuizTeamRepository quizTeamRepository) {
        this.messagingTemplate = messagingTemplate;
        this.quizSessionRepository = quizSessionRepository;
        this.quizRepository = quizRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.quizTeamRepository = quizTeamRepository;
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
}