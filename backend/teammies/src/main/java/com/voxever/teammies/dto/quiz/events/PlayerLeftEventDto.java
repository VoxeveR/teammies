package com.voxever.teammies.dto.quiz.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLeftEventDto {
    private Long playerId;
    private String playerUsername;
    private Long teamId;
    private String teamName;
    private Long newCaptainId;
    private String newCaptainUsername;
    private Boolean teamDeleted;
    private QuizEventType eventType;
}
