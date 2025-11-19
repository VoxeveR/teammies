package com.voxever.teammies.repository;

import com.voxever.teammies.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
//    List<Quiz> findByLeagueId(Long leagueId);
//
//    Optional<Quiz> findByIdAndLeagueId(Long quizId, Long leagueId);
}
