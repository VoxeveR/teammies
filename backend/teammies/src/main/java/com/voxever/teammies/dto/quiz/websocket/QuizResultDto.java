package com.voxever.teammies.dto.quiz.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long teamId;
    private String teamName;
    private Integer points;
    private Integer position;

    public QuizResultDto(Long teamId, String teamName, Integer points) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.points = points;
    }
}
