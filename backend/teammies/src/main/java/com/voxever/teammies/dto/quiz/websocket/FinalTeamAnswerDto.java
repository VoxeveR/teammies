package com.voxever.teammies.dto.quiz.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalTeamAnswerDto {
    private Long teamId;
    private String teamName;
    private Long questionId;
    private String finalAnswer;
    private Integer finalAnswerIndex;
    private String correctAnswer;
    private Integer correctAnswerIndex;
    private Boolean isCorrect;
    private String decisionMethod; // "MAJORITY", "CAPTAIN_DECISION", "RANDOM"
    private Long timestamp;
}
