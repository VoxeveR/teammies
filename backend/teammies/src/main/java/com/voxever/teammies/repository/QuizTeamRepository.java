package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voxever.teammies.entity.QuizTeam;

public interface QuizTeamRepository extends JpaRepository<QuizTeam, Long> {
    Optional<QuizTeam> findByJoinCode(String joinCode);
    List<QuizTeam> findByQuizSessionId(Long quizSessionId);
}
