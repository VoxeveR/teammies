package com.voxever.teammies.service;

import com.voxever.teammies.dto.quiz.rest.CreateQuizRequest;
import com.voxever.teammies.dto.quiz.rest.CreateQuizResponse;
import com.voxever.teammies.dto.quiz.rest.QuizResponse;
import com.voxever.teammies.dto.quiz.rest.UpdateQuizRequest;
import com.voxever.teammies.entity.*;
import com.voxever.teammies.repository.LeagueRepository;
import com.voxever.teammies.repository.QuizRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
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
                .timeLimit(request.getTimeLimit())
                .published(request.isPublished())
                .createdBy(user)
                .questions(new HashSet<>()) // initialize to prevent NPE
                .build();

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            Set<Question> questions = request.getQuestions().stream()
                    .map(this::mapQuestionDtoToEntity)
                    .peek(q -> q.setQuiz(quiz)) // back-reference
                    .collect(Collectors.toSet());
            quiz.setQuestions(questions);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return ResponseEntity.ok(mapQuizToResponse(savedQuiz));
    }

    private Question mapQuestionDtoToEntity(CreateQuizRequest.QuestionDto qDto) {
        Question question = Question.builder()
                .text(qDto.getText())
                .questionType(qDto.getQuestionType() != null ? qDto.getQuestionType() : "SINGLE_CHOICE")
                .points(qDto.getPoints() != null ? qDto.getPoints() : 1)
                .position(qDto.getPosition() != null ? qDto.getPosition() : 1)
                .answerOptions(new HashSet<>()) // initialize to prevent NPE
                .build();

        if (qDto.getAnswerOptions() != null && !qDto.getAnswerOptions().isEmpty()) {
            Set<AnswerOption> options = qDto.getAnswerOptions().stream()
                    .map(aDto -> mapAnswerOptionDtoToEntity(aDto, question))
                    .collect(Collectors.toSet());
            question.setAnswerOptions(options);
        }

        return question;
    }

    private AnswerOption mapAnswerOptionDtoToEntity(CreateQuizRequest.QuestionDto.AnswerOptionDto aDto, Question question) {
        return AnswerOption.builder()
                .question(question)
                .text(aDto.getText())
                .correct(aDto.isCorrect())
                .position(aDto.getPosition() != null ? aDto.getPosition() : 1)
                .build();
    }

    private CreateQuizResponse mapQuizToResponse(Quiz quiz) {
        Set<CreateQuizResponse.QuestionResponse> questionResponses = quiz.getQuestions() != null
                ? quiz.getQuestions().stream()
                .map(q -> {
                    Set<CreateQuizResponse.QuestionResponse.AnswerOptionResponse> answerResponses =
                            q.getAnswerOptions() != null
                                    ? q.getAnswerOptions().stream()
                                    .map(a -> CreateQuizResponse.QuestionResponse.AnswerOptionResponse.builder()
                                            .id(a.getId())
                                            .text(a.getText())
                                            .correct(a.getCorrect())
                                            .position(a.getPosition())
                                            .build())
                                    .collect(Collectors.toSet())
                                    : new HashSet<>();

                    return CreateQuizResponse.QuestionResponse.builder()
                            .id(q.getId())
                            .text(q.getText())
                            .questionType(q.getQuestionType())
                            .points(q.getPoints())
                            .position(q.getPosition())
                            .answerOptions(answerResponses)
                            .build();
                })
                .collect(Collectors.toSet())
                : new HashSet<>();

        return CreateQuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .published(quiz.isPublished())
                .questions(questionResponses)
                .build();
    }

    public ResponseEntity<List<QuizResponse>> getQuizzesByLeagueId(Long leagueId) {
        List<Quiz> quizzes = quizRepository.findByLeagueId(leagueId);

        List<QuizResponse> response = quizzes.stream().map(q -> {
            Set<QuizResponse.QuestionResponse> questions = q.getQuestions() != null
                    ? q.getQuestions().stream().map(ques -> {
                Set<QuizResponse.QuestionResponse.AnswerOptionResponse> options =
                        ques.getAnswerOptions() != null
                                ? ques.getAnswerOptions().stream()
                                .map(a -> QuizResponse.QuestionResponse.AnswerOptionResponse.builder()
                                        .id(a.getId())
                                        .text(a.getText())
                                        .correct(a.getCorrect())
                                        .position(a.getPosition())
                                        .build())
                                .collect(Collectors.toSet())
                                : Set.of();

                return QuizResponse.QuestionResponse.builder()
                        .id(ques.getId())
                        .text(ques.getText())
                        .questionType(ques.getQuestionType())
                        .points(ques.getPoints())
                        .position(ques.getPosition())
                        .answerOptions(options)
                        .build();
            }).collect(Collectors.toSet())
                    : Set.of();

            return QuizResponse.builder()
                    .id(q.getId())
                    .leagueId(q.getLeague().getId())
                    .title(q.getTitle())
                    .description(q.getDescription())
                    .published(q.isPublished())
                    .createdByUsername(q.getCreatedBy() != null ? q.getCreatedBy().getUsername() : "unknown")
                    .questions(questions)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<QuizResponse> getQuizByIdInLeague(Long leagueId, Long quizId) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        QuizResponse response = QuizResponse.builder()
                .id(quiz.getId())
                .leagueId(quiz.getLeague().getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .published(quiz.isPublished())
                .createdByUsername(quiz.getCreatedBy() != null ? quiz.getCreatedBy().getUsername() : "unknown")
                .build();

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<QuizResponse> updateQuiz(Long leagueId, Long quizId, UpdateQuizRequest request, User user) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        if (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        // Update basic quiz info
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setPublished(request.isPublished());

        // Update questions in place to avoid orphanRemoval exception
        Set<Question> existingQuestions = quiz.getQuestions() != null ? quiz.getQuestions() : new HashSet<>();
        Map<Long, Question> idToQuestion = existingQuestions.stream()
                .filter(q -> q.getId() != null)
                .collect(Collectors.toMap(Question::getId, q -> q));

        Set<Question> updatedQuestions = new HashSet<>();

        if (request.getQuestions() != null) {
            for (UpdateQuizRequest.QuestionDto qDto : request.getQuestions()) {
                Question question;

                if (qDto.getId() != null && idToQuestion.containsKey(qDto.getId())) {
                    // Existing question: update
                    question = idToQuestion.get(qDto.getId());
                    question.setText(qDto.getText());
                    question.setQuestionType(qDto.getQuestionType());
                    question.setPoints(qDto.getPoints() != null ? qDto.getPoints() : 1);
                    question.setPosition(qDto.getPosition() != null ? qDto.getPosition() : question.getPosition());

                    // Update answer options
                    Set<AnswerOption> existingOptions = question.getAnswerOptions() != null
                            ? question.getAnswerOptions()
                            : new HashSet<>();
                    existingOptions.clear();

                    if (qDto.getAnswerOptions() != null) {
                        Set<AnswerOption> newOptions = qDto.getAnswerOptions().stream()
                                .map(aDto -> AnswerOption.builder()
                                        .question(question)
                                        .text(aDto.getText())
                                        .correct(aDto.isCorrect())
                                        .position(aDto.getPosition() != null ? aDto.getPosition() : 1)
                                        .build())
                                .collect(Collectors.toSet());
                        existingOptions.addAll(newOptions);
                    }

                    question.setAnswerOptions(existingOptions);

                } else {
                    // New question: create
                    question = Question.builder()
                            .quiz(quiz)
                            .text(qDto.getText())
                            .questionType(qDto.getQuestionType())
                            .points(qDto.getPoints() != null ? qDto.getPoints() : 1)
                            .position(qDto.getPosition() != null ? qDto.getPosition() : 1)
                            .answerOptions(new HashSet<>())
                            .build();

                    if (qDto.getAnswerOptions() != null) {
                        Set<AnswerOption> options = qDto.getAnswerOptions().stream()
                                .map(aDto -> AnswerOption.builder()
                                        .question(question)
                                        .text(aDto.getText())
                                        .correct(aDto.isCorrect())
                                        .position(aDto.getPosition() != null ? aDto.getPosition() : 1)
                                        .build())
                                .collect(Collectors.toSet());
                        question.setAnswerOptions(options);
                    }
                }

                updatedQuestions.add(question);
            }
        }

        // Remove questions that were deleted
        existingQuestions.retainAll(updatedQuestions);
        existingQuestions.addAll(updatedQuestions);
        quiz.setQuestions(existingQuestions);

        // Save updated quiz
        Quiz updated = quizRepository.save(quiz);

        // Map to response
        QuizResponse response = QuizResponse.builder()
                .id(updated.getId())
                .leagueId(updated.getLeague().getId())
                .title(updated.getTitle())
                .description(updated.getDescription())
                .published(updated.isPublished())
                .createdByUsername(updated.getCreatedBy() != null ? updated.getCreatedBy().getUsername() : "unknown")
                .questions(updated.getQuestions().stream().map(q -> {
                    Set<QuizResponse.QuestionResponse.AnswerOptionResponse> answerResponses =
                            q.getAnswerOptions() != null
                                    ? q.getAnswerOptions().stream()
                                    .map(a -> QuizResponse.QuestionResponse.AnswerOptionResponse.builder()
                                            .id(a.getId())
                                            .text(a.getText())
                                            .correct(a.getCorrect())
                                            .position(a.getPosition())
                                            .build())
                                    .collect(Collectors.toSet())
                                    : Set.of();

                    return QuizResponse.QuestionResponse.builder()
                            .id(q.getId())
                            .text(q.getText())
                            .questionType(q.getQuestionType())
                            .points(q.getPoints())
                            .position(q.getPosition())
                            .answerOptions(answerResponses)
                            .build();
                }).collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(response);
    }



    public ResponseEntity<Void> deleteQuiz(Long leagueId, Long quizId, User user) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found in this league"));

        if (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this quiz");
        }

        quizRepository.delete(quiz);
        return ResponseEntity.noContent().build();
    }
}
