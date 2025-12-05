package com.voxever.teammies.dto.league;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder @Getter @Setter
@AllArgsConstructor
public class CreateLeagueResponse {
    @JsonProperty("league_id")
    private Long leagueId;
}