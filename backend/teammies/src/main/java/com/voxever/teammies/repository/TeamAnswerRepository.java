package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voxever.teammies.entity.TeamAnswer;

@Repository
public interface TeamAnswerRepository extends JpaRepository<TeamAnswer, Long> {
    Optional<TeamAnswer> findByTeamIdAndQuestionId(Long teamId, Long questionId);
    List<TeamAnswer> findByTeamId(Long teamId);
    List<TeamAnswer> findByQuestionId(Long questionId);
}
