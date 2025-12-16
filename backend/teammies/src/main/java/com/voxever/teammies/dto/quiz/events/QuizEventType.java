package com.voxever.teammies.dto.quiz.events;

public enum QuizEventType {
    TEAM_CREATED("TEAM_CREATED"),
    PLAYER_JOINED("PLAYER_JOINED"),
    QUESTION_SENT("QUESTION_SENT"),
    TIMER_STARTED("TIMER_STARTED"),
    QUIZ_ENDED("QUIZ_ENDED"),
    PLAYER_ANSWERED("PLAYER_ANSWERED"),
    SESSION_CLOSED("SESSION_CLOSED");

    private final String value;

    QuizEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}