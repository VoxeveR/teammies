package com.voxever.teammies.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.dto.quiz.rest.GenerateJoinCodeResponseDto;
import com.voxever.teammies.dto.quiz.rest.JoinQuizRequestDto;
import com.voxever.teammies.dto.quiz.rest.JoinQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.StartQuizResponseDto;
import com.voxever.teammies.dto.quiz.rest.TeamWithPlayersDto;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.dto.quiz.websocket.QuizResultDto;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.LeagueStanding;
import com.voxever.teammies.entity.Question;
import com.voxever.teammies.entity.Quiz;
import com.voxever.teammies.entity.QuizPlayer;
import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.entity.Team;
import com.voxever.teammies.entity.TeamAnswer;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueStandingRepository;
import com.voxever.teammies.repository.QuestionRepository;
import com.voxever.teammies.repository.QuizPlayerRepository;
import com.voxever.teammies.repository.QuizRepository;
import com.voxever.teammies.repository.QuizSessionRepository;
import com.voxever.teammies.repository.QuizTeamRepository;
import com.voxever.teammies.repository.TeamAnswerRepository;
import com.voxever.teammies.repository.TeamRepository;
import com.voxever.teammies.service.mapper.FinalAnswerMapper;
import com.voxever.teammies.service.mapper.QuestionEventMapper;
import com.voxever.teammies.service.mapper.QuizResultMapper;
import com.voxever.teammies.service.mapper.QuizTeamMapper;

import jakarta.transaction.Transactional;

@Service
public class QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuizTeamRepository quizTeamRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final QuestionRepository questionRepository;
    private final TeamAnswerRepository teamAnswerRepository;
    private final LeagueStandingRepository leagueStandingRepository;
    private final TeamRepository teamRepository;
    private final Random random = new Random();
    private final QuizSessionWebSocketBroadcasts webSocketService;
    private final TaskScheduler taskScheduler;
    private final QuestionEventMapper questionEventMapper;
    private final QuizTeamMapper quizTeamMapper;
    private final QuizResultMapper quizResultMapper;
    private final FinalAnswerMapper finalAnswerMapper;

    public QuizSessionService(QuizSessionRepository quizSessionRepository,
                              QuizRepository quizRepository,
                              QuizTeamRepository quizTeamRepository,
                              QuizPlayerRepository quizPlayerRepository,
                              QuestionRepository questionRepository,
                              TeamAnswerRepository teamAnswerRepository,
                              LeagueStandingRepository leagueStandingRepository,
                              TeamRepository teamRepository,
                              QuizSessionWebSocketBroadcasts webSocketService,
                              TaskScheduler taskScheduler,
                              QuestionEventMapper questionEventMapper,
                              QuizTeamMapper quizTeamMapper,
                              QuizResultMapper quizResultMapper,
                              FinalAnswerMapper finalAnswerMapper) {
        this.quizSessionRepository = quizSessionRepository;
        this.quizRepository = quizRepository;
        this.quizTeamRepository = quizTeamRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.questionRepository = questionRepository;
        this.teamAnswerRepository = teamAnswerRepository;
        this.leagueStandingRepository = leagueStandingRepository;
        this.teamRepository = teamRepository;
        this.webSocketService = webSocketService;
        this.taskScheduler = taskScheduler;
        this.questionEventMapper = questionEventMapper;
        this.quizTeamMapper = quizTeamMapper;
        this.quizResultMapper = quizResultMapper;
        this.finalAnswerMapper = finalAnswerMapper;
    }


    @Transactional
    public ResponseEntity<GenerateJoinCodeResponseDto> generateJoinCode(Long leagueId, Long quizId) {
        Quiz quiz = quizRepository.findByIdAndLeagueId(quizId, leagueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz not found"));

        var existingSession = quizSessionRepository.findByQuizIdAndStatus(quizId, QuizSession.SessionStatus.WAITING);
        if (existingSession.isPresent()) {
            QuizSession session = existingSession.get();
            return ResponseEntity.ok(GenerateJoinCodeResponseDto.builder()
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

        return ResponseEntity.ok(GenerateJoinCodeResponseDto.builder()
                .quizSessionId(savedSession.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .joinCode(savedSession.getJoinCode())
                .build());
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (quizSessionRepository.findByJoinCode(code).isPresent());
        return code;
    }

    @Transactional
    public ResponseEntity<StartQuizResponseDto> startQuiz(String sessionJoinCode, User user) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        validateQuizStart(session, user);
        
        List<QuizTeam> teams = quizTeamRepository.findByQuizSessionId(session.getId());
        validateTeams(teams);

        QuizSession updatedSession = activateSession(session);
        Quiz quiz = updatedSession.getQuiz();
        Question firstQuestion = getFirstQuestion(quiz);

        broadcastQuizStart(sessionJoinCode, firstQuestion, quiz);
        scheduleNextQuestion(sessionJoinCode, firstQuestion.getPosition(), quiz.getTimeLimit());

        return buildStartQuizResponse(updatedSession, quiz, teams);
    }

    private void validateQuizStart(QuizSession session, User user) {
        if (!session.getQuiz().getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the quiz creator can start the session");
        }
        if (session.getStatus() != QuizSession.SessionStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz session is not in WAITING status");
        }
    }

    private void validateTeams(List<QuizTeam> teams) {
        if (teams.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start quiz without teams");
        }
        for (QuizTeam team : teams) {
            if (team.getPlayers() == null || team.getPlayers().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Team '" + team.getName() + "' has no players. All teams must have at least one player.");
            }
        }
    }

    private QuizSession activateSession(QuizSession session) {
        session.setStatus(QuizSession.SessionStatus.IN_PROGRESS);
        return quizSessionRepository.save(session);
    }

    private Question getFirstQuestion(Quiz quiz) {
        return quiz.getQuestions().stream()
                .min(Comparator.comparingInt(Question::getPosition))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz has no questions"));
    }

    private void broadcastQuizStart(String sessionJoinCode, Question firstQuestion, Quiz quiz) {
        QuestionEventDto questionEvent = questionEventMapper.mapToQuestionEventDto(firstQuestion, quiz, quiz.getQuestions().size());
        webSocketService.broadcastQuizStarted(sessionJoinCode, questionEvent);
    }

    private ResponseEntity<StartQuizResponseDto> buildStartQuizResponse(QuizSession session, Quiz quiz, List<QuizTeam> teams) {
        int totalPlayers = teams.stream().mapToInt(team -> team.getPlayers().size()).sum();
        List<StartQuizResponseDto.TeamStartInfo> teamInfos = teams.stream().map(quizTeamMapper::mapToTeamStartInfo).toList();

        return ResponseEntity.ok(StartQuizResponseDto.builder()
                .quizSessionId(session.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .sessionJoinCode(session.getJoinCode())
                .teamCount(teams.size())
                .totalPlayerCount(totalPlayers)
                .startedAt(session.getUpdatedAt())
                .teams(teamInfos)
                .build());
    }

    private void scheduleNextQuestion(String sessionJoinCode, int currentPosition, int timeLimit) {
        System.out.println("Scheduling final answer calculation and next question");
        taskScheduler.schedule(
                () -> calculateAndBroadcastFinalAnswers(sessionJoinCode, currentPosition),
                Instant.now().plusSeconds(timeLimit + 1)
        );
    }

    @Transactional
    protected void calculateAndBroadcastFinalAnswers(String sessionJoinCode, int currentPosition) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow();

        if (session.getStatus() != QuizSession.SessionStatus.IN_PROGRESS) {
            return;
        }

        List<QuizTeam> teams = quizTeamRepository.findByQuizSessionId(session.getId());
        Question currentQuestion = quizRepository.findByIdWithQuestions(session.getQuiz().getId())
                .orElseThrow()
                .getQuestions().stream()
                .filter(q -> q.getPosition() == currentPosition)
                .findFirst()
                .orElseThrow();

        for (QuizTeam team : teams) {
            try {
                FinalTeamAnswerDto finalAnswer = calculateAndSaveFinalTeamAnswer(team.getId(), currentQuestion.getId());

                webSocketService.broadcastFinalAnswer(sessionJoinCode, team.getJoinCode(), finalAnswer);
            } catch (Exception e) {
                System.err.println("Error calculating final answer for team " + team.getId() + ": " + e.getMessage());
            }
        }

        taskScheduler.schedule(
                () -> sendNextQuestion(sessionJoinCode, currentPosition),
                Instant.now().plusSeconds(2)
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
            finishQuiz(sessionJoinCode, session, quiz);
            return;
        }

        broadcastQuestion(sessionJoinCode, nextQuestionOpt.get(), quiz);
        scheduleNextQuestion(sessionJoinCode, nextQuestionOpt.get().getPosition(), quiz.getTimeLimit());
    }

    private void finishQuiz(String sessionJoinCode, QuizSession session, Quiz quiz) {
        List<QuizTeam> quizTeams = quizTeamRepository.findByQuizSessionId(session.getId());
        Set<Long> quizQuestionIds = quiz.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
        Map<Long, String> correctAnswers = quizResultMapper.buildCorrectAnswersMap(quiz);
        List<QuizResultDto> resultsWithPosition = quizResultMapper.mapToQuizResultDtos(quizTeams, quiz, quizQuestionIds, correctAnswers);
        
        webSocketService.broadcastQuizResults(sessionJoinCode, resultsWithPosition);
        updateLeagueStandings(quiz.getLeague(), resultsWithPosition);
        
        webSocketService.broadcastQuizEnded(sessionJoinCode);
        session.setStatus(QuizSession.SessionStatus.FINISHED);
        quizSessionRepository.save(session);
    }

    private void updateLeagueStandings(League league, List<QuizResultDto> results) {
        for (QuizResultDto result : results) {
            try {
                QuizTeam quizTeam = quizTeamRepository.findById(result.getTeamId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz team not found"));

                Team team = teamRepository.findByLeagueAndName(league, quizTeam.getName())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found in league: " + quizTeam.getName()));

                LeagueStanding standing = leagueStandingRepository.findByLeagueAndTeam(league, team)
                        .orElse(LeagueStanding.builder()
                                .league(league)
                                .team(team)
                                .points(0)
                                .matchesPlayed(0)
                                .build());

                standing.setPoints(standing.getPoints() + result.getPoints());
                standing.setMatchesPlayed(standing.getMatchesPlayed() + 1);
                leagueStandingRepository.save(standing);
            } catch (Exception e) {
                System.err.println("Error updating LeagueStanding for team " + result.getTeamId() + ": " + e.getMessage());
            }
        }
    }

    private void broadcastQuestion(String sessionJoinCode, Question question, Quiz quiz) {
        QuestionEventDto questionEvent = questionEventMapper.mapToQuestionEventDto(question, quiz, quiz.getQuestions().size());
        webSocketService.broadcastQuestion(sessionJoinCode, questionEvent);
    }

    @Transactional
    public ResponseEntity<JoinQuizResponseDto> joinQuiz(JoinQuizRequestDto request) {
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

        List<JoinQuizResponseDto.TeamInfoDto> teamInfos = teams.stream()
                .map(quizTeamMapper::mapToTeamInfoDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(JoinQuizResponseDto.builder()
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
                .map(quizTeamMapper::mapToTeamWithPlayersDto)
                .toList();

        return ResponseEntity.ok(teams);
    }



    @Transactional
    public FinalTeamAnswerDto calculateAndSaveFinalTeamAnswer(Long teamId, Long questionId) {
        QuizTeam team = quizTeamRepository.findByIdWithPlayers(teamId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));

        // Get all players in the team
        Set<QuizPlayer> players = team.getPlayers();

        if (players == null || players.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has no players");
        }

        // Count votes for each answer option from player highlights (selections)
        Map<String, Integer> voteCount = new HashMap<>();
        Map<String, Integer> indexMap = new HashMap<>();
        QuizPlayer captain = null;
        String captainAnswer = null;
        Integer captainAnswerIndex = null;

        for (QuizPlayer player : players) {
            // Check if player has made a selection for this question
            if (player.getCurrentHighlight() != null && 
                player.getCurrentQuestionId() != null && 
                player.getCurrentQuestionId().equals(questionId)) {
                
                String answer = player.getCurrentHighlight();
                voteCount.put(answer, voteCount.getOrDefault(answer, 0) + 1);
                if (player.getCurrentHighlightIndex() != null) {
                    indexMap.put(answer, player.getCurrentHighlightIndex());
                }
            }

            if (player.isCaptain()) {
                captain = player;
                // Store captain's answer if they voted
                if (player.getCurrentQuestionId() != null && 
                    player.getCurrentQuestionId().equals(questionId) &&
                    player.getCurrentHighlight() != null) {
                    captainAnswer = player.getCurrentHighlight();
                    captainAnswerIndex = player.getCurrentHighlightIndex();
                }
            }
        }

        // Determine final answer based on voting logic
        String finalAnswer;
        Integer finalAnswerIndex;
        String decisionMethod;

        if (voteCount.isEmpty()) {
            // No votes cast - set answer to null with RANDOM method
            finalAnswer = null;
            finalAnswerIndex = -1;
            decisionMethod = "RANDOM";
        } else {
            // Find the answer with the most votes
            String topAnswer = voteCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            int maxVotes = voteCount.get(topAnswer);

            // Count how many answers have the max vote count (tie)
            long tieCount = voteCount.values().stream()
                    .filter(count -> count == maxVotes)
                    .count();

            if (tieCount == 1) {
                // Clear winner by majority
                finalAnswer = topAnswer;
                finalAnswerIndex = indexMap.getOrDefault(topAnswer, -1);
                decisionMethod = "MAJORITY";
            } else {
                // Tie detected - need captain or random
                if (captain != null && captainAnswer != null) {
                    // Captain breaks the tie
                    finalAnswer = captainAnswer;
                    finalAnswerIndex = captainAnswerIndex != null ? captainAnswerIndex : -1;
                    decisionMethod = "CAPTAIN_DECISION";
                } else {
                    // Random selection among tied answers
                    List<String> tiedAnswers = voteCount.entrySet().stream()
                            .filter(e -> e.getValue() == maxVotes)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    finalAnswer = tiedAnswers.get(random.nextInt(tiedAnswers.size()));
                    finalAnswerIndex = indexMap.getOrDefault(finalAnswer, -1);
                    decisionMethod = "RANDOM";
                }
            }
        }

        // Save the final team answer to database
        TeamAnswer teamAnswer = TeamAnswer.builder()
                .team(team)
                .question(question)
                .finalAnswer(finalAnswer)
                .finalAnswerIndex(finalAnswerIndex)
                .decisionMethod(TeamAnswer.DecisionMethod.valueOf(decisionMethod))
                .build();

        teamAnswerRepository.save(teamAnswer);

        // Get the correct answer from the question and build response DTO
        FinalTeamAnswerDto finalTeamAnswerDto = finalAnswerMapper.mapToFinalTeamAnswerDto(
                teamId, team.getName(), questionId, finalAnswer, finalAnswerIndex, question, decisionMethod
        );

        return finalTeamAnswerDto;
    }

    @Transactional
    public ResponseEntity<Void> closeQuizSession(String sessionJoinCode, User user) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Quiz session not found"));

        if (!session.getQuiz().getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the quiz creator can close the session");
        }

        session.setStatus(QuizSession.SessionStatus.FINISHED);
        quizSessionRepository.save(session);

        webSocketService.broadcastSessionClosed(sessionJoinCode);

        return ResponseEntity.ok().build();
    }
}

