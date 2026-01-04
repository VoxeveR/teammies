package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voxever.teammies.entity.QuizPlayer;

public interface QuizPlayerRepository extends JpaRepository<QuizPlayer, Long> {
    List<QuizPlayer> findByTeamId(Long teamId);

    @Query("SELECT qp FROM QuizPlayer qp WHERE qp.nickname = :nickname AND qp.team.quizSession.id = :sessionId")
    Optional<QuizPlayer> findByNicknameAndSessionId(@Param("nickname") String nickname, @Param("sessionId") Long sessionId);
}
