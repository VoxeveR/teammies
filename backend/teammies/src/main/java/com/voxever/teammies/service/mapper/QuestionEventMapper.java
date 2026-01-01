package com.voxever.teammies.service.mapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.voxever.teammies.dto.quiz.events.QuestionEventDto;
import com.voxever.teammies.entity.Question;
import com.voxever.teammies.entity.Quiz;

@Component
public class QuestionEventMapper {

    public QuestionEventDto mapToQuestionEventDto(Question question, Quiz quiz, int totalQuestions) {
        return QuestionEventDto.builder()
                .eventType(com.voxever.teammies.dto.quiz.events.QuizEventType.QUESTION_SENT)
                .questionId(question.getId())
                .questionText(question.getText())
                .questionType(question.getQuestionType())
                .points(question.getPoints())
                .position(question.getPosition())
                .questionTime(quiz.getTimeLimit())
                .questionTimeTimestamp(Instant.now().plus(Duration.ofSeconds(quiz.getTimeLimit())))
                .totalQuestions(totalQuestions)
                .answerOptions(question.getAnswerOptions().stream()
                        .sorted(Comparator.comparingInt(com.voxever.teammies.entity.AnswerOption::getPosition))
                        .map(option -> QuestionEventDto.AnswerOptionDto.builder()
                                .id(option.getId())
                                .text(option.getText())
                                .position(option.getPosition())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
