package com.voxever.teammies.service;

import com.voxever.teammies.dto.quiz.events.QuizEventType;
import com.voxever.teammies.dto.quiz.rest.GenerateJoinCodeResponse;
import com.voxever.teammies.dto.quiz.rest.JoinQuizRequest;
import com.voxever.teammies.dto.quiz.rest.JoinQuizResponse;
import com.voxever.teammies.dto.quiz.rest.TeamWithPlayersDto;
import com.voxever.teammies.dto.quiz.rest.StartQuizResponse;
import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.entity.Quiz;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.voxever.teammies.entity.Question;

@Service
public class QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuizTeamRepository quizTeamRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final Random random = new Random();
    private final QuizSessionWebSocketBroadcasts webSocketService;
    private final TaskScheduler taskScheduler;

public QuizSessionService(QuizSessionRepository quizSessionRepository,
                          QuizRepository quizRepository,
                          QuizTeamRepository quizTeamRepository,
                          QuizPlayerRepository quizPlayerRepository,
                          QuizSessionWebSocketBroadcasts webSocketService, 
                          TaskScheduler taskScheduler) {
    this.quizSessionRepository = quizSessionRepository;
    this.quizRepository = quizRepository;
    this.quizTeamRepository = quizTeamRepository;
    this.quizPlayerRepository = quizPlayerRepository;
    this.webSocketService = webSocketService;
    this.taskScheduler = taskScheduler;
}
    

    @Transactional
    public ResponseEntity<GenerateJoinCodeResponse> generateJoinCode(Long leagueId, Long quizId) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found"));

        // Check if session already exists for this quiz
        var existingSession = quizSessionRepository.findByQuizIdAndStatus(quizId, QuizSession.SessionStatus.WAITING);
        if (existingSession.isPresent()) {
            QuizSession session = existingSession.get();
            return ResponseEntity.ok(GenerateJoinCodeResponse.builder()
                    .quizSessionId(session.getId())
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .joinCode(session.getJoinCode())
                    .build());
        }

        String joinCode = generateUniqueJoinCode();

        QuizSession session = QuizSession.builder()
                .quiz(quiz)
                .joinCode(joinCode)
                .status(QuizSession.SessionStatus.WAITING)
                .build();

        QuizSession savedSession = quizSessionRepository.save(session);

        return ResponseEntity.ok(GenerateJoinCodeResponse.builder()
                .quizSessionId(savedSession.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .joinCode(savedSession.getJoinCode())
                .build());
    }

     @Transactional
    public ResponseEntity<StartQuizResponse> startQuiz(String sessionJoinCode, User user) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        if (!session.getQuiz().getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the quiz creator can start the session");
        }

        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz session is not in WAITING status");
        }

        List<QuizTeam> teams = quizTeamRepository.findByQuizSessionId(session.getId());
        if (teams.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start quiz without teams");
        }

        for (QuizTeam team : teams) {
            if (team.getPlayers() == null || team.getPlayers().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Team '" + team.getName() + "' has no players. All teams must have at least one player.");
            }
        }

        session.setStatus(QuizSession.SessionStatus.IN_PROGRESS);
        QuizSession updatedSession = quizSessionRepository.save(session);

        Quiz quiz = updatedSession.getQuiz();
        Question firstQuestion = quiz.getQuestions().stream()
                .min((q1, q2) -> Integer.compare(q1.getPosition(), q2.getPosition()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz has no questions"));

        int totalQuestions = quiz.getQuestions().size();

        QuestionEventDto questionEvent = QuestionEventDto.builder()
                .eventType(QuizEventType.QUESTION_SENT)
                .questionId(firstQuestion.getId())
                .questionText(firstQuestion.getText())
                .questionType(firstQuestion.getQuestionType())
                .points(firstQuestion.getPoints())
                .position(firstQuestion.getPosition())
                .questionTime(quiz.getTimeLimit())
                .questionTimeTimestamp(Instant.now().plus(Duration.ofSeconds(quiz.getTimeLimit())))
                .totalQuestions(totalQuestions)
                .answerOptions(firstQuestion.getAnswerOptions().stream()
                        .map(option -> QuestionEventDto.AnswerOptionDto.builder()
                                .id(option.getId())
                                .text(option.getText())
                                .position(option.getPosition())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        webSocketService.broadcastQuizStarted(sessionJoinCode, questionEvent);

        scheduleNextQuestion(sessionJoinCode, firstQuestion.getPosition(), quiz.getTimeLimit());

        int totalPlayers = teams.stream()
                .mapToInt(team -> team.getPlayers().size())
                .sum();

        List<StartQuizResponse.TeamStartInfo> teamInfos = teams.stream()
                .map(team -> StartQuizResponse.TeamStartInfo.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .playerCount(team.getPlayers().size())
                        .build())
                .toList();

        return ResponseEntity.ok(StartQuizResponse.builder()
                .quizSessionId(updatedSession.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .sessionJoinCode(updatedSession.getJoinCode())
                .teamCount(teams.size())
                .totalPlayerCount(totalPlayers)
                .startedAt(updatedSession.getUpdatedAt())
                .teams(teamInfos)
                .build());
    }

    private void scheduleNextQuestion(String sessionJoinCode, int currentPosition, int timeLimit) {
        System.out.println("Sending next question");
        taskScheduler.schedule(
                () -> sendNextQuestion(sessionJoinCode, currentPosition),
                Instant.now().plusSeconds(timeLimit + 3)
        );
    }

    @Transactional
    public void sendNextQuestion(String sessionJoinCode, int currentPosition) {

        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow();

        if (session.getStatus() != QuizSession.SessionStatus.IN_PROGRESS) {
            return;
        }

        Quiz quiz = quizRepository.findByIdWithQuestions(session.getQuiz().getId())
                .orElseThrow();


        Optional<Question> nextQuestionOpt = quiz.getQuestions().stream()
                .filter(q -> q.getPosition() > currentPosition)
                .min(Comparator.comparingInt(Question::getPosition));

        if (nextQuestionOpt.isEmpty()) {
            webSocketService.broadcastQuizEnded(sessionJoinCode);
            session.setStatus(QuizSession.SessionStatus.FINISHED);
            quizSessionRepository.save(session);
            return;
        }

        Question nextQuestion = nextQuestionOpt.get();

        QuestionEventDto questionEvent = QuestionEventDto.builder()
                .eventType(QuizEventType.QUESTION_SENT)
                .questionId(nextQuestion.getId())
                .questionText(nextQuestion.getText())
                .questionType(nextQuestion.getQuestionType())
                .points(nextQuestion.getPoints())
                .position(nextQuestion.getPosition())
                .questionTime(quiz.getTimeLimit())
                .questionTimeTimestamp(
                        Instant.now().plusSeconds(quiz.getTimeLimit())
                )
                .totalQuestions(quiz.getQuestions().size())
                .answerOptions(nextQuestion.getAnswerOptions().stream()
                        .map(option -> QuestionEventDto.AnswerOptionDto.builder()
                                .id(option.getId())
                                .text(option.getText())
                                .position(option.getPosition())
                                .build())
                        .toList())
                .build();

        webSocketService.broadcastQuestion(sessionJoinCode, questionEvent);

        // ⏭ zaplanuj następne
        scheduleNextQuestion(
                sessionJoinCode,
                nextQuestion.getPosition(),
                quiz.getTimeLimit()
        );
    }

    @Transactional
    public ResponseEntity<JoinQuizResponse> joinQuiz(JoinQuizRequest request) {
        QuizSession session = quizSessionRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Invalid join code"));

        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz session is not available for joining");
        }

        Quiz quiz = session.getQuiz();
        List<QuizTeam> teams = quizTeamRepository.findByQuizSessionId(session.getId());

        QuizPlayer tempPlayer = QuizPlayer.builder()
                .nickname(request.getUsername())
                .isCaptain(false)
                .build();
        QuizPlayer savedPlayer = quizPlayerRepository.save(tempPlayer);

        List<JoinQuizResponse.TeamInfoDto> teamInfos = teams.stream()
                .map(team -> JoinQuizResponse.TeamInfoDto.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .teamJoinCode(team.getJoinCode())
                        .playerCount(team.getPlayers() != null ? team.getPlayers().size() : 0)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(JoinQuizResponse.builder()
                .quizSessionId(session.getId())
                .sessionJoinCode(session.getJoinCode())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .quizDescription(quiz.getDescription())
                .quizPlayerId(savedPlayer.getId())
                .username(request.getUsername())
                .availableTeams(teamInfos)
                .build());
    }

    @Transactional
    public ResponseEntity<List<TeamWithPlayersDto>> getAllTeamsWithPlayers(String sessionJoinCode) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        List<TeamWithPlayersDto> teams = session.getTeams().stream()
                .map(team -> TeamWithPlayersDto.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .teamJoinCode(team.getJoinCode())
                        .players(team.getPlayers().stream()
                                .map(player -> TeamWithPlayersDto.PlayerInfoDto.builder()
                                        .playerId(player.getId())
                                        .playerUsername(player.getNickname())
                                        .isCaptain(player.isCaptain())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return ResponseEntity.ok(teams);
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (quizSessionRepository.findByJoinCode(code).isPresent());
        return code;
    }
}
