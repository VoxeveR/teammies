package com.voxever.teammies.dto.league;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateLeagueRequest {

    @JsonProperty("league_name")
    private String leagueName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("team_size")
    private Integer teamSize;

    @JsonProperty("max_teams")
    private Integer maxTeams;

    @JsonProperty("is_public")
    private Boolean isPublic;
}