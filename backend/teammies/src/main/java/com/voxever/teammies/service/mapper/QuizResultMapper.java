package com.voxever.teammies.service.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.voxever.teammies.dto.quiz.websocket.QuizResultDto;
import com.voxever.teammies.entity.Question;
import com.voxever.teammies.entity.Quiz;
import com.voxever.teammies.entity.QuizTeam;
import com.voxever.teammies.entity.TeamAnswer;
import com.voxever.teammies.repository.TeamAnswerRepository;

@Component
public class QuizResultMapper {

    private final TeamAnswerRepository teamAnswerRepository;

    public QuizResultMapper(TeamAnswerRepository teamAnswerRepository) {
        this.teamAnswerRepository = teamAnswerRepository;
    }

    public Map<Long, String> buildCorrectAnswersMap(Quiz quiz) {
        return quiz.getQuestions().stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> q.getAnswerOptions().stream()
                                .filter(com.voxever.teammies.entity.AnswerOption::getCorrect)
                                .map(com.voxever.teammies.entity.AnswerOption::getText)
                                .findFirst()
                                .orElse(null)
                ));
    }

    public List<QuizResultDto> mapToQuizResultDtos(List<QuizTeam> quizTeams, Quiz quiz, 
                                                     Set<Long> quizQuestionIds, Map<Long, String> correctAnswers) {
        List<QuizResultDto> results = quizTeams.stream()
                .map(quizTeam -> {
                    long correctCount = teamAnswerRepository.findByTeamId(quizTeam.getId()).stream()
                            .filter(answer -> isCorrectAnswer(answer, quizQuestionIds, correctAnswers))
                            .count();
                    int points = (int) (correctCount * 10); // 10 points per correct answer
                    return new QuizResultDto(quizTeam.getId(), quizTeam.getName(), points);
                })
                .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
                .collect(Collectors.toList());

        // Add position to each result
        return IntStream.range(0, results.size())
                .mapToObj(i -> {
                    QuizResultDto result = results.get(i);
                    result.setPosition(i + 1);
                    return result;
                })
                .collect(Collectors.toList());
    }

    private boolean isCorrectAnswer(TeamAnswer answer, Set<Long> quizQuestionIds, Map<Long, String> correctAnswers) {
        Long questionId = answer.getQuestion().getId();
        if (!quizQuestionIds.contains(questionId)) return false;

        String correctText = correctAnswers.get(questionId);
        return answer.getFinalAnswer() != null && answer.getFinalAnswer().equals(correctText);
    }
}
