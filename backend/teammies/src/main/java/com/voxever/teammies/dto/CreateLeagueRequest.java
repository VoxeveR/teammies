package com.voxever.teammies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CreateLeagueRequest {

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
