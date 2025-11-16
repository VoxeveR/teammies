package com.voxever.teammies.service;

import com.voxever.teammies.dto.CreateLeagueRequest;
import com.voxever.teammies.dto.CreateLeagueResponse;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class LeagueService {

    private LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public ResponseEntity<CreateLeagueResponse> createLeague(CreateLeagueRequest request, User user){
        System.out.println(user.getUserId());
        return ResponseEntity.ok().build();
    }

    private void generateLeagueJoinCode(){}

    private void deleteLeague(){}

}
