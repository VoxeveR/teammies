package com.voxever.teammies.dto.quiz.rest;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class QuizResponseDto {
    private Long id;
    private Long leagueId;
    private String title;
    private String description;
    private boolean published;
    private String createdByUsername;

    private Set<QuestionResponse> questions; // new field

    @Data
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String text;
        private String questionType;
        private Integer points;
        private Integer position;
        private Set<AnswerOptionResponse> answerOptions;

        @Data
        @Builder
        public static class AnswerOptionResponse {
            private Long id;
            private String text;
            private Boolean correct;
            private Integer position;
        }
    }
}
