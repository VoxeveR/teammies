package com.voxever.teammies.service;

import com.voxever.teammies.dto.quiz.CreateQuizRequest;
import com.voxever.teammies.dto.quiz.CreateQuizResponse;
import com.voxever.teammies.dto.quiz.QuizResponse;
import com.voxever.teammies.dto.quiz.UpdateQuizRequest;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.Quiz;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import com.voxever.teammies.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final LeagueRepository leagueRepository;

    public QuizService(QuizRepository quizRepository, LeagueRepository leagueRepository) {
        this.quizRepository = quizRepository;
        this.leagueRepository = leagueRepository;
    }

    // CREATE
    public ResponseEntity<CreateQuizResponse> createQuiz(Long leagueId, CreateQuizRequest request, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));

        Quiz quiz = Quiz.builder()
                .league(league)
                .title(request.getTitle())
                .description(request.getDescription())
                .published(request.isPublished())
                .createdBy(user)
                .build();

        Quiz saved = quizRepository.save(quiz);

        CreateQuizResponse response = CreateQuizResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .published(saved.isPublished())
                .build();

        return ResponseEntity.ok().body(response);
    }

    // READ - wszystkie quizy w lidze
    public ResponseEntity<List<QuizResponse>> getQuizzesByLeagueId(Long leagueId) {
        List<Quiz> quizzes = quizRepository.findByLeagueId(leagueId);

        List<QuizResponse> response = quizzes.stream().map(q ->
                QuizResponse.builder()
                        .id(q.getId())
                        .leagueId(q.getLeague().getLeagueId())
                        .title(q.getTitle())
                        .description(q.getDescription())
                        .published(q.isPublished())
                        .createdByUsername(q.getCreatedBy().getUsername())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

//    // READ - pojedynczy quiz w lidze
//    public ResponseEntity<QuizResponse> getQuizByIdInLeague(Long leagueId, Long quizId) {
//        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
//                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));
//
//        QuizResponse response = QuizResponse.builder()
//                .id(quiz.getId())
//                .leagueId(quiz.getLeague().getId())
//                .title(quiz.getTitle())
//                .description(quiz.getDescription())
//                .published(quiz.isPublished())
//                .createdByUsername(quiz.getCreatedBy().getUsername())
//                .build();
//
//        return ResponseEntity.ok(response);
//    }

    // UPDATE
    public ResponseEntity<QuizResponse> updateQuiz(Long leagueId, Long quizId, UpdateQuizRequest request, User user) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found in this league"));

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setPublished(request.isPublished());

        Quiz updated = quizRepository.save(quiz);

        QuizResponse response = QuizResponse.builder()
                .id(updated.getId())
                .leagueId(updated.getLeague().getLeagueId())
                .title(updated.getTitle())
                .description(updated.getDescription())
                .published(updated.isPublished())
                .createdByUsername(updated.getCreatedBy().getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    // DELETE
    public ResponseEntity<Void> deleteQuiz(Long leagueId, Long quizId, User user) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found in this league"));

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        quizRepository.delete(quiz);
        return ResponseEntity.noContent().build();
    }
}
