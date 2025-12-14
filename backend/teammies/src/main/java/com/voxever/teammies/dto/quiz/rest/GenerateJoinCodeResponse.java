package com.voxever.teammies.dto.quiz.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateJoinCodeResponse {
    private Long quizSessionId;
    private Long quizId;
    private String quizTitle;
    private String joinCode;
}
