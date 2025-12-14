package com.voxever.teammies.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voxever.teammies.dto.quiz.rest.CreateQuizRequest;
import com.voxever.teammies.dto.quiz.rest.CreateQuizResponse;
import com.voxever.teammies.dto.quiz.rest.GenerateJoinCodeResponse;
import com.voxever.teammies.dto.quiz.rest.QuizResponse;
import com.voxever.teammies.dto.quiz.rest.UpdateQuizRequest;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.service.QuizService;
import com.voxever.teammies.service.QuizSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leagues/{leagueId}/quizzes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizController {

    private final QuizService quizService;
    private final QuizSessionService quizSessionService;

    public QuizController(QuizService quizService, QuizSessionService quizSessionService) {
        this.quizService = quizService;
        this.quizSessionService = quizSessionService;
    }

    @PostMapping
    public ResponseEntity<CreateQuizResponse> createQuiz(
            @PathVariable Long leagueId,
            @RequestBody @Valid CreateQuizRequest request,
            @AuthenticationPrincipal User user) {
        return quizService.createQuiz(leagueId, request, user);
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAllQuizzesInLeague(@PathVariable Long leagueId) {
        return quizService.getQuizzesByLeagueId(leagueId);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable Long leagueId,
            @PathVariable Long quizId) {
        return quizService.getQuizByIdInLeague(leagueId, quizId);
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long leagueId,
            @PathVariable Long quizId,
            @RequestBody @Valid UpdateQuizRequest request,
            @AuthenticationPrincipal User user) {
        return quizService.updateQuiz(leagueId, quizId, request, user);
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long leagueId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal User user) {
        return quizService.deleteQuiz(leagueId, quizId, user);
    }

    @PostMapping("/{quizId}/generate-join-code")
    public ResponseEntity<GenerateJoinCodeResponse> generateJoinCode(
            @PathVariable Long leagueId,
            @PathVariable Long quizId) {
        return quizSessionService.generateJoinCode(leagueId, quizId);
    }
}
