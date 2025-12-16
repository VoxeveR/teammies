package com.voxever.teammies.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByLeagueAndName(League league, String name);
}
