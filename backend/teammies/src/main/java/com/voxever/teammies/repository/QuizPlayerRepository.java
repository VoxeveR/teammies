package com.voxever.teammies.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voxever.teammies.entity.QuizPlayer;

public interface QuizPlayerRepository extends JpaRepository<QuizPlayer, Long> {
    List<QuizPlayer> findByTeamId(Long teamId);
}
