package com.voxever.teammies.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    @NotBlank(message = "Team name is required")
    @Size(min = 1, max = 100, message = "Team name must be between 1 and 100 characters")
    private String teamName;

    @NotNull(message = "Quiz player ID is required")
    private Long quizPlayerId;
}
