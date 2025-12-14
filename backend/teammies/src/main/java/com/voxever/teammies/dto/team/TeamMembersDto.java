package com.voxever.teammies.dto.team;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMembersDto {
    private Long teamId;
    private String teamName;
    private List<TeamMemberInfoDto> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberInfoDto {
        private Long playerId;
        private String nickname;
        private boolean isCaptain;
    }
}
