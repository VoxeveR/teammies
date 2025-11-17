package com.voxever.teammies.repository;

import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueRepository extends JpaRepository<League, Long> {

    Boolean existsByLeagueName(String leagueName);

    List<League> findAllByUserId(Long userId);
}
