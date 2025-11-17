package com.voxever.teammies.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private Long leagueId;
    private String title;
    private String description;
    private String createdByUsername;
    private boolean published;
}