package com.voxever.teammies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeagueResponse {

    @JsonProperty("league_id")
    private Long leagueId;

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

    @JsonProperty("owner_id")
    private Long ownerId;
}