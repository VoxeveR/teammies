package com.voxever.teammies.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuizRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    private boolean published;

    private List<QuestionDto> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDto {
        private Long id;
        private String text;
        private String questionType; // "SINGLE_CHOICE"
        private Integer points;
        private Integer position;
        private List<AnswerOptionDto> answerOptions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerOptionDto {
        private Long id;
        private String text;
        private boolean correct;
        private Integer position;
    }
}
