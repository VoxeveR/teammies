package com.voxever.teammies.dto.quiz.rest;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {
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
    }
}
