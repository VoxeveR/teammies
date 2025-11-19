package com.voxever.teammies.controller;

import com.voxever.teammies.dto.quiz.CreateQuizRequest;
import com.voxever.teammies.dto.quiz.CreateQuizResponse;
import com.voxever.teammies.dto.quiz.QuizResponse;
import com.voxever.teammies.dto.quiz.UpdateQuizRequest;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues/{leagueId}/quizzes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

//    @PostMapping
//    public ResponseEntity<CreateQuizResponse> createQuiz(
//            @PathVariable Long leagueId,
//            @RequestBody @Valid CreateQuizRequest request,
//            @AuthenticationPrincipal User user) {
//        return quizService.createQuiz(leagueId, request, user);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<QuizResponse>> getAllQuizzesInLeague(@PathVariable Long leagueId) {
//        return quizService.getQuizzesByLeagueId(leagueId);
//    }
//
////    @GetMapping("/{quizId}")
////    public ResponseEntity<QuizResponse> getQuizById(
////            @PathVariable Long leagueId,
////            @PathVariable Long quizId) {
////        return quizService.getQuizByIdInLeague(leagueId, quizId);
////    }
//
//    @PutMapping("/{quizId}")
//    public ResponseEntity<QuizResponse> updateQuiz(
//            @PathVariable Long leagueId,
//            @PathVariable Long quizId,
//            @RequestBody @Valid UpdateQuizRequest request,
//            @AuthenticationPrincipal User user) {
//        return quizService.updateQuiz(leagueId, quizId, request, user);
//    }
//
//    @DeleteMapping("/{quizId}")
//    public ResponseEntity<Void> deleteQuiz(
//            @PathVariable Long leagueId,
//            @PathVariable Long quizId,
//            @AuthenticationPrincipal User user) {
//        return quizService.deleteQuiz(leagueId, quizId, user);
//    }
}
