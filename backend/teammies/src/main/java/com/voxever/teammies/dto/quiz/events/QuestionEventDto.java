package com.voxever.teammies.dto.quiz.events;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEventDto {
    private QuizEventType eventType;
    private Long questionId;
    private String questionText;
    private String questionType;
    private Integer points;
    private Integer position;
    private Integer totalQuestions;
    private Integer questionTime;
    private Instant questionTimeTimestamp;
    private List<AnswerOptionDto> answerOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerOptionDto {
        private Long id;
        private String text;
        private Integer position;
    }
}
