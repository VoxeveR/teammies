package com.voxever.teammies.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuizResponse {
    private Long id;
    private String title;
    private String description;
    private boolean published;
    private Set<QuestionResponse> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String text;
        private String questionType;
        private Integer points;
        private Integer position;
        private Set<AnswerOptionResponse> answerOptions;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class AnswerOptionResponse {
            private Long id;
            private String text;
            private boolean correct;
            private Integer position;
        }
    }
}
