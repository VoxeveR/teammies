package com.voxever.teammies.service;

import com.voxever.teammies.dto.quiz.CreateQuizRequest;
import com.voxever.teammies.dto.quiz.CreateQuizResponse;
import com.voxever.teammies.dto.quiz.QuizResponse;
import com.voxever.teammies.dto.quiz.UpdateQuizRequest;
import com.voxever.teammies.entity.*;
import com.voxever.teammies.repository.LeagueRepository;
import com.voxever.teammies.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final LeagueRepository leagueRepository;

    public QuizService(QuizRepository quizRepository, LeagueRepository leagueRepository) {
        this.quizRepository = quizRepository;
        this.leagueRepository = leagueRepository;
    }

    public ResponseEntity<CreateQuizResponse> createQuiz(Long leagueId, CreateQuizRequest request, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "League not found"));

        Quiz quiz = Quiz.builder()
                .league(league)
                .title(request.getTitle())
                .description(request.getDescription())
                .published(request.isPublished())
                .createdBy(user)
                .build();

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            Set<Question> questions = request.getQuestions().stream()
                    .map(this::mapQuestionDtoToEntity)
                    .peek(q -> q.setQuiz(quiz)) // ensure back-reference
                    .collect(Collectors.toSet());
            quiz.setQuestions(questions);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return ResponseEntity.ok(mapQuizToResponse(savedQuiz));
    }

    // Map a QuestionDto to Question entity
    private Question mapQuestionDtoToEntity(CreateQuizRequest.QuestionDto qDto) {
        Question question = Question.builder()
                .text(qDto.getText())
                .questionType(qDto.getQuestionType())
                .points(qDto.getPoints())
                .position(qDto.getPosition())
                .build();

        if (qDto.getAnswerOptions() != null && !qDto.getAnswerOptions().isEmpty()) {
            Set<AnswerOption> options = qDto.getAnswerOptions().stream()
                    .map(aDto -> mapAnswerOptionDtoToEntity(aDto, question))
                    .collect(Collectors.toSet());
            question.setAnswerOptions(options);
        }

        return question;
    }

    // Map an AnswerOptionDto to AnswerOption entity
    private AnswerOption mapAnswerOptionDtoToEntity(CreateQuizRequest.QuestionDto.AnswerOptionDto aDto, Question question) {
        return AnswerOption.builder()
                .question(question)
                .text(aDto.getText())
                .correct(aDto.isCorrect())
                .position(aDto.getPosition()) // important for NOT NULL
                .build();
    }

    // Map saved Quiz entity to CreateQuizResponse
    private CreateQuizResponse mapQuizToResponse(Quiz quiz) {
        Set<CreateQuizResponse.QuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> {
                    Set<CreateQuizResponse.QuestionResponse.AnswerOptionResponse> answerResponses =
                            q.getAnswerOptions().stream()
                                    .map(a -> CreateQuizResponse.QuestionResponse.AnswerOptionResponse.builder()
                                            .id(a.getId())
                                            .text(a.getText())
                                            .correct(a.getCorrect())
                                            .position(a.getPosition())
                                            .build())
                                    .collect(Collectors.toSet());

                    return CreateQuizResponse.QuestionResponse.builder()
                            .id(q.getId())
                            .text(q.getText())
                            .questionType(q.getQuestionType())
                            .points(q.getPoints())
                            .position(q.getPosition())
                            .answerOptions(answerResponses)
                            .build();
                })
                .collect(Collectors.toSet());

        return CreateQuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .published(quiz.isPublished())
                .questions(questionResponses)
                .build();
    }


    // READ - wszystkie quizy w lidze
    public ResponseEntity<List<QuizResponse>> getQuizzesByLeagueId(Long leagueId) {
        List<Quiz> quizzes = quizRepository.findByLeagueId(leagueId);

        List<QuizResponse> response = quizzes.stream().map(q ->
                QuizResponse.builder()
                        .id(q.getId())
                        .leagueId(q.getLeague().getId())
                        .title(q.getTitle())
                        .description(q.getDescription())
                        .published(q.isPublished())
                        .createdByUsername(q.getCreatedBy().getUsername())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // READ - pojedynczy quiz w lidze
    public ResponseEntity<QuizResponse> getQuizByIdInLeague(Long leagueId, Long quizId) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        QuizResponse response = QuizResponse.builder()
                .id(quiz.getId())
                .leagueId(quiz.getLeague().getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .published(quiz.isPublished())
                .createdByUsername(quiz.getCreatedBy().getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    // UPDATE
    public ResponseEntity<QuizResponse> updateQuiz(Long leagueId, Long quizId, UpdateQuizRequest request, User user) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setPublished(request.isPublished());

        Quiz updated = quizRepository.save(quiz);

        QuizResponse response = QuizResponse.builder()
                .id(updated.getId())
                .leagueId(updated.getLeague().getId())
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
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        quizRepository.delete(quiz);
        return ResponseEntity.noContent().build();
    }
}
