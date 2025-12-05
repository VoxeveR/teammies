package com.voxever.teammies.dto.league;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStandingResponseDto {
    private String teamName;
    private Integer points;
    private Integer matchesPlayed;
}
