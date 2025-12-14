package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voxever.teammies.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLeagueId(Long leagueId);

    Optional<Quiz> findByIdAndLeagueId(Long quizId, Long leagueId);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
}
