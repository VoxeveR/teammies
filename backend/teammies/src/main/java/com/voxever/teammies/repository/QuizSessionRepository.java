package com.voxever.teammies.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voxever.teammies.entity.QuizSession;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByJoinCode(String joinCode);
    Optional<QuizSession> findByQuizIdAndStatus(Long quizId, QuizSession.SessionStatus status);
    
    @Query("SELECT q.createdBy.userId FROM QuizSession qs JOIN qs.quiz q WHERE qs.joinCode = :joinCode")
    Optional<Long> findQuizOrganizerIdBySessionJoinCode(@Param("joinCode") String joinCode);
}
