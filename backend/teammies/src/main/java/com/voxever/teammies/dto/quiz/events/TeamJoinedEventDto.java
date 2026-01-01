package com.voxever.teammies.dto.quiz.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamJoinedEventDto {
    private Long teamId;
    private String teamName;
    private String teamJoinCode;
    private Integer memberCount;
    private QuizEventType eventType;
}