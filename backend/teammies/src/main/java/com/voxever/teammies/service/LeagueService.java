package com.voxever.teammies.service;

import com.voxever.teammies.dto.league.CreateLeagueRequest;
import com.voxever.teammies.dto.league.CreateLeagueResponse;
import com.voxever.teammies.dto.league.LeagueResponse;
import com.voxever.teammies.dto.league.UpdateLeagueRequest;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


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

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateLeagueResponse.builder().leagueId(createdLeague.getId()).build());
    }

    @Transactional
    public ResponseEntity<LeagueResponse> updateLeague(Long id, UpdateLeagueRequest request, User user) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));

        if (!league.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update");
        }

        if (request.getLeagueName() != null &&
                !request.getLeagueName().equals(league.getLeagueName()) &&
                leagueRepository.existsByLeagueName(request.getLeagueName())) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "League with that name already exists");
        }

        if (request.getLeagueName() != null) league.setLeagueName(request.getLeagueName());
        if (request.getDescription() != null) league.setDescription(request.getDescription());
        if (request.getTeamSize() != null) league.setTeamSize(request.getTeamSize());
        if (request.getMaxTeams() != null) league.setMaxTeams(request.getMaxTeams());
        if (request.getIsPublic() != null) league.setIsPublic(request.getIsPublic());

        leagueRepository.save(league);

        return ResponseEntity.ok(toLeagueResponse(league));
    }

    public ResponseEntity<List<LeagueResponse>> getLeaguesOwnedByUser(User user) {
        List<LeagueResponse> leagues = leagueRepository.findAllByUser(user).stream()
                .map(this::toLeagueResponse)
                .toList();

        return ResponseEntity.ok().body(leagues);
    }

    private LeagueResponse toLeagueResponse(League league) {
        return LeagueResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getLeagueName())
                .description(league.getDescription())
                .teamSize(league.getTeamSize())
                .maxTeams(league.getMaxTeams())
                .isPublic(league.getIsPublic())
                .ownerId(league.getUser() != null ? league.getUser().getUserId() : null)
                .build();
    }

    @Transactional
    public ResponseEntity<Void> deleteLeague(Long id, User user) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));

        if (!league.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete");
        }

        leagueRepository.delete(league);
        return ResponseEntity.ok().build();
    }
}
