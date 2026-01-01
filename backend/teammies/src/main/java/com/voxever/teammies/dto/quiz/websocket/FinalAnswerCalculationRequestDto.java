package com.voxever.teammies.dto.quiz.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalAnswerCalculationRequestDto {
    private Long teamId;
    private Long questionId;
}
