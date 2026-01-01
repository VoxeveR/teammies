package com.voxever.teammies.service.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.voxever.teammies.dto.quiz.rest.JoinQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.StartQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.TeamWithPlayersDto;
import com.voxever.teammies.entity.QuizTeam;

@Component
public class QuizTeamMapper {

    public StartQuizResponseDto.TeamStartInfo mapToTeamStartInfo(QuizTeam team) {
        return StartQuizResponseDto.TeamStartInfo.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .playerCount(team.getPlayers().size())
                .build();
    }

    public JoinQuizResponseDto.TeamInfoDto mapToTeamInfoDto(QuizTeam team) {
        return JoinQuizResponseDto.TeamInfoDto.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .teamJoinCode(team.getJoinCode())
                .playerCount(team.getPlayers() != null ? team.getPlayers().size() : 0)
                .build();
    }

    public TeamWithPlayersDto mapToTeamWithPlayersDto(QuizTeam team) {
        return TeamWithPlayersDto.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .teamJoinCode(team.getJoinCode())
                .players(team.getPlayers().stream()
                        .map(player -> TeamWithPlayersDto.PlayerInfoDto.builder()
                                .playerId(player.getId())
                                .playerUsername(player.getNickname())
                                .isCaptain(player.isCaptain())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
