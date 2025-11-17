package com.voxever.teammies.service;

import com.voxever.teammies.dto.CreateLeagueRequest;
import com.voxever.teammies.dto.CreateLeagueResponse;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeagueService {

    private LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public ResponseEntity<CreateLeagueResponse> createLeague(CreateLeagueRequest request, User user){

        if(leagueRepository.existsByLeagueName(request.getLeagueName()))  {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "League with that name already exists");
        }

        League createdLeague = League.builder()
                .leagueName(request.getLeagueName())
                .description(request.getDescription())
                .maxTeams(request.getMaxTeams())
                .isPublic(request.getIsPublic())
                .teamSize(request.getTeamSize())
                .user(user).build();

        leagueRepository.save(createdLeague);

        return ResponseEntity.ok().body(CreateLeagueResponse.builder().build());
    }

    private void generateLeagueJoinCode(){}

    private void deleteLeague(){}

}
