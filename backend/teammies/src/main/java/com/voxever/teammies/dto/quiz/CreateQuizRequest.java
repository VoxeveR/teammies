package com.voxever.teammies.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuizRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    private boolean published;

    private Set<QuestionDto> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDto {
        @NotBlank
        private String text;

        @NotBlank
        private String questionType;

        @NotNull
        private Integer points;

        @NotNull
        private Integer position;

        private Set<AnswerOptionDto> answerOptions;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class AnswerOptionDto {
            @NotBlank
            private String text;

            private boolean correct;

            @NotNull
            private Integer position; // required for DB NOT NULL
        }
    }
}
