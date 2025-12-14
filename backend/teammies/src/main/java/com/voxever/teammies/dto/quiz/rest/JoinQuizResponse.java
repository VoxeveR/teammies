package com.voxever.teammies.dto.quiz.rest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinQuizResponse {
    private Long quizSessionId;
    private String sessionJoinCode;
    private Long quizId;
    private String quizTitle;
    private String quizDescription;
    private Long quizPlayerId;
    private String username;
    private List<TeamInfoDto> availableTeams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInfoDto {
        private Long teamId;
        private String teamName;
        private String teamJoinCode;
        private int playerCount;
    }
}
