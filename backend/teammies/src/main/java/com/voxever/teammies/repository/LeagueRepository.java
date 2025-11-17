package com.voxever.teammies.repository;

import com.voxever.teammies.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {

    Boolean existsByLeagueName(String leagueName);
}
