package com.voxever.teammies.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voxever.teammies.entity.QuizSession;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByJoinCode(String joinCode);
    Optional<QuizSession> findByQuizIdAndStatus(Long quizId, QuizSession.SessionStatus status);
}
