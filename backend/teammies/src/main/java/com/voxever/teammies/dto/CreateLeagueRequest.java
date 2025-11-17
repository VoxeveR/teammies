package com.voxever.teammies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CreateLeagueRequest {

    @JsonProperty("league_name")
    @NotBlank()
    private String leagueName;

    @JsonProperty("description")
    @NotBlank()
    private String description;

    @JsonProperty("team_size")
    @Min(0)
    @NotNull()
    private Integer teamSize;

    @JsonProperty("max_teams")
    @NotNull()
    @Min(0)
    private Integer maxTeams;

    @JsonProperty("is_public")
    @NotNull()
    private Boolean isPublic;
}
