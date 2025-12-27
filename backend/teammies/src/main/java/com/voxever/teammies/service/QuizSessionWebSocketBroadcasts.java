package com.voxever.teammies.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.voxever.teammies.dto.quiz.QuizResultDto;
import com.voxever.teammies.dto.quiz.events.PlayerJoinedEvent;
import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.dto.quiz.events.QuizEventType;
import com.voxever.teammies.dto.quiz.events.TeamJoinedEvent;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizTeam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuizSessionWebSocketBroadcasts {

    private final SimpMessagingTemplate messagingTemplate;

    public QuizSessionWebSocketBroadcasts(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastTeamCreated(String sessionJoinCode, QuizTeam team) {
        log.info("Broadcasting team created: {} for session: {}", team.getName(), sessionJoinCode);
        
        TeamJoinedEvent event = TeamJoinedEvent.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .teamJoinCode(team.getJoinCode())
                .memberCount(team.getPlayers() != null ? team.getPlayers().size() : 0)
                .eventType(QuizEventType.TEAM_CREATED)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                event
        );
    }

    public void broadcastPlayerJoined(String sessionJoinCode, QuizTeam team, QuizPlayer player) {
        log.info("Broadcasting player joined: {} in team: {} for session: {}", 
                player.getNickname(), team.getName(), sessionJoinCode);
        
        PlayerJoinedEvent event = PlayerJoinedEvent.builder()
                .playerId(player.getId())
                .playerUsername(player.getNickname())
                .teamId(team.getId())
                .teamName(team.getName())
                .isCaptain(player.isCaptain())
                .eventType(QuizEventType.PLAYER_JOINED)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                event
        );
    }

    public void broadcastQuizStarted(String sessionJoinCode, QuestionEventDto firstQuestion) {
        log.info("Broadcasting quiz started with first question for session: {}", sessionJoinCode);
        
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/events",
                firstQuestion
        );
    }

    public void broadcastQuizEnded(String sessionJoinCode) {
        log.info("Broadcasting quiz ended for session: {}", sessionJoinCode);
        
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                PlayerJoinedEvent.builder()
                        .eventType(QuizEventType.QUIZ_ENDED)
                        .build()
        );
    }

    public void broadcastQuestion(String sessionJoinCode, QuestionEventDto questionEvent) {
        log.info("Broadcasting next quiz question for session: {}", sessionJoinCode);
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/events",
                questionEvent
        );
    }

    public void broadcastFinalAnswer(String sessionJoinCode, String teamCode, FinalTeamAnswerDto finalAnswer) {
        log.info("Broadcasting final answer for team {} in session: {}", teamCode, sessionJoinCode);
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/team/" + teamCode + "/final-answer",
                finalAnswer
        );
    }

    public void broadcastQuizResults(String sessionJoinCode, List<QuizResultDto> results) {
        log.info("Broadcasting quiz results for session: {} with {} teams", sessionJoinCode, results.size());
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/results",
                results
        );
    }

    public void broadcastSessionClosed(String sessionJoinCode) {
        log.info("Broadcasting session closed for session: {}", sessionJoinCode);
        
        PlayerJoinedEvent event = PlayerJoinedEvent.builder()
                .eventType(QuizEventType.SESSION_CLOSED)
                .build();

        // Broadcast to all members in the session
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/events",
                event
        );

        // Also broadcast to admin
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                event
        );
    }
}