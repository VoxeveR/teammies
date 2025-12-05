package com.voxever.teammies.repository;

import com.voxever.teammies.entity.LeagueStanding;
import com.voxever.teammies.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueStandingRepository extends JpaRepository<LeagueStanding, Long> {
    List<LeagueStanding> findAllByLeagueOrderByPointsDescMatchesPlayedAsc(League league);
}