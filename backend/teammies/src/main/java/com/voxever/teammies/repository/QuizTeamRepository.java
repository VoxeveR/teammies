package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voxever.teammies.entity.QuizTeam;

public interface QuizTeamRepository extends JpaRepository<QuizTeam, Long> {
    Optional<QuizTeam> findByJoinCode(String joinCode);
    List<QuizTeam> findByQuizSessionId(Long quizSessionId);
    
    @Query("SELECT DISTINCT t FROM QuizTeam t LEFT JOIN FETCH t.players WHERE t.id = :id")
    Optional<QuizTeam> findByIdWithPlayers(@Param("id") Long id);
}
