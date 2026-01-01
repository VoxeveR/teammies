package com.voxever.teammies.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voxever.teammies.dto.quiz.rest.JoinQuizRequestDto;
import com.voxever.teammies.dto.quiz.rest.JoinQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.StartQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.TeamWithPlayersDto;
import com.voxever.teammies.dto.team.CreateTeamRequest;
import com.voxever.teammies.dto.team.CreateTeamResponse;
import com.voxever.teammies.dto.team.JoinTeamRequest;
import com.voxever.teammies.dto.team.JoinTeamResponse;
import com.voxever.teammies.dto.team.TeamMembersDto;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.service.QuizSessionService;
import com.voxever.teammies.service.QuizTeamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quiz-sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizSessionController {

    private final QuizTeamService quizTeamService;
    private final QuizSessionService quizSessionService;

    public QuizSessionController(QuizTeamService quizTeamService, QuizSessionService quizSessionService) {
        this.quizTeamService = quizTeamService;
        this.quizSessionService = quizSessionService;
    }

    @PostMapping("/{sessionJoinCode}/start")
    public ResponseEntity<StartQuizResponseDto> startQuiz(
            @PathVariable String sessionJoinCode,
            @AuthenticationPrincipal User user) {
        return quizSessionService.startQuiz(sessionJoinCode, user);
    }

    @PostMapping("/{sessionJoinCode}/close")
    public ResponseEntity<Void> closeQuizSession(
            @PathVariable String sessionJoinCode,
            @AuthenticationPrincipal User user) {
        return quizSessionService.closeQuizSession(sessionJoinCode, user);
    }


    @GetMapping("/{sessionJoinCode}/teams/all")
    public ResponseEntity<List<TeamWithPlayersDto>> getAllTeams(
            @PathVariable String sessionJoinCode) {
        return quizSessionService.getAllTeamsWithPlayers(sessionJoinCode);
    }

    @PostMapping("/join")
    public ResponseEntity<JoinQuizResponseDto> joinQuiz(@RequestBody @Valid JoinQuizRequestDto request) {
        return quizSessionService.joinQuiz(request);
    }

    @PostMapping("/{sessionJoinCode}/teams")
    public ResponseEntity<CreateTeamResponse> createTeam(
            @PathVariable String sessionJoinCode,
            @RequestBody @Valid CreateTeamRequest request) {
        return quizTeamService.createTeam(sessionJoinCode, request);
    }

    @PostMapping("/{sessionJoinCode}/teams/join")
    public ResponseEntity<JoinTeamResponse> joinTeam(
            @PathVariable String sessionJoinCode,
            @RequestBody @Valid JoinTeamRequest request) {
        return quizTeamService.joinTeam(sessionJoinCode, request);
    }

    @GetMapping("/{sessionJoinCode}/teams/{teamJoinCode}/members")
    public ResponseEntity<TeamMembersDto> getTeamMembers(
            @PathVariable String sessionJoinCode,
            @PathVariable String teamJoinCode) {
        return quizTeamService.getTeamMembers(sessionJoinCode, teamJoinCode);
    }
}
