package com.voxever.teammies.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamResponse {
    private Long teamId;
    private String teamName;
    private String teamJoinCode;
    private Long quizSessionId;
    private String playerUsername;
    private boolean isCaptain;
    private String message;
}
