package com.voxever.teammies.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.voxever.teammies.dto.quiz.events.PlayerJoinedEventDto;
import com.voxever.teammies.dto.quiz.events.PlayerLeftEventDto;
import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.dto.quiz.events.QuizEventType;
import com.voxever.teammies.dto.quiz.events.TeamJoinedEventDto;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.dto.quiz.websocket.QuizResultDto;
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
        
        TeamJoinedEventDto event = TeamJoinedEventDto.builder()
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
        
        PlayerJoinedEventDto event = PlayerJoinedEventDto.builder()
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

    public void broadcastPlayerLeft(String sessionJoinCode, QuizTeam team, QuizPlayer leftPlayer, 
                                    QuizPlayer newCaptain, boolean teamDeleted) {
        log.info("=== BROADCASTING PLAYER LEFT ===");
        log.info("Broadcasting player left: {} from team: {} for session: {}", 
                leftPlayer.getNickname(), team.getName(), sessionJoinCode);
        log.info("Team deleted: {}", teamDeleted);
        log.info("New captain: {}", newCaptain != null ? newCaptain.getNickname() : "NONE");
        
        PlayerLeftEventDto event = PlayerLeftEventDto.builder()
                .playerId(leftPlayer.getId())
                .playerUsername(leftPlayer.getNickname())
                .teamId(team.getId())
                .teamName(team.getName())
                .newCaptainId(newCaptain != null ? newCaptain.getId() : null)
                .newCaptainUsername(newCaptain != null ? newCaptain.getNickname() : null)
                .teamDeleted(teamDeleted)
                .eventType(QuizEventType.PLAYER_LEFT)
                .build();

        log.info("Sending to admin channel: /topic/quiz-session/{}/admin/events", sessionJoinCode);
        // Broadcast to admin
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                event
        );
        log.info("Admin broadcast sent");

        // Broadcast to team members (if team still exists)
        if (!teamDeleted) {
            String teamChannelTopic = "/topic/quiz-session/" + sessionJoinCode + "/team/" + team.getJoinCode() + "/events";
            log.info("Sending to team channel: {}", teamChannelTopic);
            messagingTemplate.convertAndSend(
                    teamChannelTopic,
                    event
            );
            log.info("Team broadcast sent");
        } else {
            log.info("Team was deleted, skipping team broadcast");
        }
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
                PlayerJoinedEventDto.builder()
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
        
        PlayerJoinedEventDto event = PlayerJoinedEventDto.builder()
                .eventType(QuizEventType.SESSION_CLOSED)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/events",
                event
        );
        
        messagingTemplate.convertAndSend(
                "/topic/quiz-session/" + sessionJoinCode + "/admin/events",
                event
        );
    }
}