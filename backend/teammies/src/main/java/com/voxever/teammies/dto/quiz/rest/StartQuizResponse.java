package com.voxever.teammies.dto.quiz.rest;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizResponse {
    private Long quizSessionId;
    private Long quizId;
    private String quizTitle;
    private String sessionJoinCode;
    private int teamCount;
    private int totalPlayerCount;
    private Instant startedAt;
    private List<TeamStartInfo> teams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamStartInfo {
        private Long teamId;
        private String teamName;
        private int playerCount;
    }
}
