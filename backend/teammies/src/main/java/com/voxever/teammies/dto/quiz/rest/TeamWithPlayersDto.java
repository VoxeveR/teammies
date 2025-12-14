// TeamWithPlayersDto.java
package com.voxever.teammies.dto.quiz.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamWithPlayersDto {
    private Long teamId;
    private String teamName;
    private String teamJoinCode;
    private List<PlayerInfoDto> players;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerInfoDto {
        private Long playerId;
        private String playerUsername;
        private boolean isCaptain;
    }
}