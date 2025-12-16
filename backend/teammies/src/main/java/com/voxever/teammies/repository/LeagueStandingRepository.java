package com.voxever.teammies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.LeagueStanding;
import com.voxever.teammies.entity.Team;

public interface LeagueStandingRepository extends JpaRepository<LeagueStanding, Long> {
    List<LeagueStanding> findAllByLeagueOrderByPointsDescMatchesPlayedAsc(League league);
    Optional<LeagueStanding> findByLeagueAndTeam(League league, Team team);
}