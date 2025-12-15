package com.voxever.teammies.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.voxever.teammies.dto.quiz.QuizResultDto;
import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.dto.quiz.events.QuizEventType;
import com.voxever.teammies.dto.quiz.rest.GenerateJoinCodeResponse;
import com.voxever.teammies.dto.quiz.rest.JoinQuizRequest;
import com.voxever.teammies.dto.quiz.rest.JoinQuizResponse;
import com.voxever.teammies.dto.quiz.rest.StartQuizResponse;
import com.voxever.teammies.dto.quiz.rest.TeamWithPlayersDto;
import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.entity.AnswerOption;
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

    public QuizSessionService(QuizSessionRepository quizSessionRepository,
                              QuizRepository quizRepository,
                              QuizTeamRepository quizTeamRepository,
                              QuizPlayerRepository quizPlayerRepository,
                              QuestionRepository questionRepository,
                              TeamAnswerRepository teamAnswerRepository,
                              LeagueStandingRepository leagueStandingRepository,
                              TeamRepository teamRepository,
                              QuizSessionWebSocketBroadcasts webSocketService,
                              TaskScheduler taskScheduler) {
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
                        .sorted(Comparator.comparingInt(AnswerOption::getPosition))
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
        System.out.println("Scheduling final answer calculation and next question");
        // Schedule to calculate final answers after time expires
        taskScheduler.schedule(
                () -> calculateAndBroadcastFinalAnswers(sessionJoinCode, currentPosition),
                Instant.now().plusSeconds(timeLimit + 1)
        );
    }

    @Transactional
    private void calculateAndBroadcastFinalAnswers(String sessionJoinCode, int currentPosition) {
        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow();

        if (session.getStatus() != QuizSession.SessionStatus.IN_PROGRESS) {
            return;
        }

        // Get all teams and their final answers
        List<QuizTeam> teams = quizTeamRepository.findByQuizSessionId(session.getId());
        Question currentQuestion = quizRepository.findByIdWithQuestions(session.getQuiz().getId())
                .orElseThrow()
                .getQuestions().stream()
                .filter(q -> q.getPosition() == currentPosition)
                .findFirst()
                .orElseThrow();

        // Calculate and broadcast final answer for each team
        for (QuizTeam team : teams) {
            try {
                FinalTeamAnswerDto finalAnswer = calculateAndSaveFinalTeamAnswer(team.getId(), currentQuestion.getId());

                // Broadcast the final answer to the team
                webSocketService.broadcastFinalAnswer(sessionJoinCode, team.getJoinCode(), finalAnswer);
            } catch (Exception e) {
                System.err.println("Error calculating final answer for team " + team.getId() + ": " + e.getMessage());
            }
        }

        // Schedule sending next question after 2 seconds
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
            // Quiz has ended - calculate and broadcast results
            List<QuizTeam> quizTeams = quizTeamRepository.findByQuizSessionId(session.getId());
            
            // Get all question IDs from current quiz for filtering
            Set<Long> quizQuestionIds = quiz.getQuestions().stream()
                    .map(Question::getId)
                    .collect(Collectors.toSet());
            
            // Build a map of question ID -> correct answer text
            Map<Long, String> correctAnswers = quiz.getQuestions().stream()
                    .collect(Collectors.toMap(
                            Question::getId,
                            q -> q.getAnswerOptions().stream()
                                    .filter(AnswerOption::getCorrect)
                                    .map(AnswerOption::getText)
                                    .findFirst()
                                    .orElse(null)
                    ));
            
            // Calculate points for each team based on correct answers in this quiz
            List<QuizResultDto> results = quizTeams.stream()
                    .map(quizTeam -> {
                        long correctCount = teamAnswerRepository.findByTeamId(quizTeam.getId()).stream()
                                .filter(answer -> {
                                    // Use the precomputed correct answers map
                                    Long questionId = answer.getQuestion().getId();
                                    if (!quizQuestionIds.contains(questionId)) return false;
                                    
                                    String correctText = correctAnswers.get(questionId);
                                    return answer.getFinalAnswer() != null && answer.getFinalAnswer().equals(correctText);
                                })
                                .count();
                        int points = (int) (correctCount * 10); // 10 points per correct answer
                        return new QuizResultDto(quizTeam.getId(), quizTeam.getName(), points);
                    })
                    .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints())) // Sort by points descending
                    .collect(Collectors.toList());
            
            // Add position to each result
            List<QuizResultDto> resultsWithPosition = IntStream.range(0, results.size())
                    .mapToObj(i -> {
                        QuizResultDto result = results.get(i);
                        result.setPosition(i + 1);
                        return result;
                    })
                    .collect(Collectors.toList());
            
            webSocketService.broadcastQuizResults(sessionJoinCode, resultsWithPosition);
            
            // Update LeagueStanding with quiz results
            League league = quiz.getLeague();
            for (QuizResultDto result : resultsWithPosition) {
                // Find the persistent Team by league and quizTeam name
                Team team = teamRepository.findByLeagueAndName(league, result.getTeamName())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team not found"));
                
                // Find existing LeagueStanding or create new one
                LeagueStanding standing = leagueStandingRepository.findByLeagueAndTeam(league, team)
                        .orElse(LeagueStanding.builder()
                                .league(league)
                                .team(team)
                                .points(0)
                                .matchesPlayed(0)
                                .build());
                
                // Update points and match count
                standing.setPoints(standing.getPoints() + result.getPoints());
                standing.setMatchesPlayed(standing.getMatchesPlayed() + 1);
                
                leagueStandingRepository.save(standing);
            }
            
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
                        .sorted(Comparator.comparingInt(AnswerOption::getPosition))
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
            finalAnswerIndex = null;
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
                finalAnswerIndex = indexMap.get(topAnswer);
                decisionMethod = "MAJORITY";
            } else {
                // Tie detected - need captain or random
                if (captain != null && captainAnswer != null) {
                    // Captain breaks the tie
                    finalAnswer = captainAnswer;
                    finalAnswerIndex = captainAnswerIndex;
                    decisionMethod = "CAPTAIN_DECISION";
                } else {
                    // Random selection among tied answers
                    List<String> tiedAnswers = voteCount.entrySet().stream()
                            .filter(e -> e.getValue() == maxVotes)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    finalAnswer = tiedAnswers.get(random.nextInt(tiedAnswers.size()));
                    finalAnswerIndex = indexMap.get(finalAnswer);
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

        // Get the correct answer from the question
        AnswerOption correctOption = question.getAnswerOptions().stream()
                .filter(AnswerOption::getCorrect)
                .findFirst()
                .orElse(null);

        String correctAnswer = correctOption != null ? correctOption.getText() : null;
        

        Integer correctAnswerIndex = null;
        if (correctOption != null) {
            List<AnswerOption> sortedOptions = question.getAnswerOptions().stream()
                    .sorted(Comparator.comparingInt(AnswerOption::getPosition))
                    .collect(Collectors.toList());
            correctAnswerIndex = IntStream.range(0, sortedOptions.size())
                    .filter(i -> sortedOptions.get(i).getId().equals(correctOption.getId()))
                    .findFirst()
                    .orElse(-1);
        }
        

        Boolean isCorrect = finalAnswer != null && correctAnswer != null && finalAnswer.equals(correctAnswer);


        FinalTeamAnswerDto finalTeamAnswerDto = FinalTeamAnswerDto.builder()
                .teamId(teamId)
                .teamName(team.getName())
                .questionId(questionId)
                .finalAnswer(finalAnswer)
                .finalAnswerIndex(finalAnswerIndex)
                .correctAnswer(correctAnswer)
                .correctAnswerIndex(correctAnswerIndex)
                .isCorrect(isCorrect)
                .decisionMethod(decisionMethod)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        return finalTeamAnswerDto;
    }
}

