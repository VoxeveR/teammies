package com.voxever.teammies.dto.league;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AllLeaguesResponse {

    @JsonProperty("my_leagues")
    private List<LeagueResponse> myLeagues;

    @JsonProperty("public_leagues")
    private List<LeagueResponse> publicLeagues;
}
