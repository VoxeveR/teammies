package com.voxever.teammies.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinTeamRequest {
    @NotNull(message = "Quiz player ID is required")
    private Long quizPlayerId;

    @NotNull(message = "Team join code is required")
    private String teamJoinCode;
}
