package com.voxever.teammies.service.mapper;

import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.voxever.teammies.dto.quiz.websocket.FinalTeamAnswerDto;
import com.voxever.teammies.entity.AnswerOption;
import com.voxever.teammies.entity.Question;

@Component
public class FinalAnswerMapper {

    public FinalTeamAnswerDto mapToFinalTeamAnswerDto(Long teamId, String teamName, Long questionId,
                                                       String finalAnswer, Integer finalAnswerIndex,
                                                       Question question, String decisionMethod) {
        AnswerOption correctOption = question.getAnswerOptions().stream()
                .filter(AnswerOption::getCorrect)
                .findFirst()
                .orElse(null);

        String correctAnswer = correctOption != null ? correctOption.getText() : null;
        Integer correctAnswerIndex = calculateCorrectAnswerIndex(question, correctOption);
        Boolean isCorrect = finalAnswer != null && correctAnswer != null && finalAnswer.equals(correctAnswer);

        return FinalTeamAnswerDto.builder()
                .teamId(teamId)
                .teamName(teamName)
                .questionId(questionId)
                .finalAnswer(finalAnswer)
                .finalAnswerIndex(finalAnswerIndex)
                .correctAnswer(correctAnswer)
                .correctAnswerIndex(correctAnswerIndex)
                .isCorrect(isCorrect)
                .decisionMethod(decisionMethod)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    private Integer calculateCorrectAnswerIndex(Question question, AnswerOption correctOption) {
        if (correctOption == null) return null;

        java.util.List<AnswerOption> sortedOptions = question.getAnswerOptions().stream()
                .sorted(Comparator.comparingInt(AnswerOption::getPosition))
                .collect(Collectors.toList());

        return IntStream.range(0, sortedOptions.size())
                .filter(i -> sortedOptions.get(i).getId().equals(correctOption.getId()))
                .findFirst()
                .orElse(-1);
    }
}
