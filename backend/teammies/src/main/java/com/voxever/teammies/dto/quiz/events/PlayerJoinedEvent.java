package com.voxever.teammies.dto.quiz.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerJoinedEvent {
    private Long playerId;
    private String playerUsername;
    private Long teamId;
    private String teamName;
    private Boolean isCaptain;
    private QuizEventType eventType;
}