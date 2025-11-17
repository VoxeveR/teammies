package com.voxever.teammies.service;

import com.voxever.teammies.dto.CreateLeagueRequest;
import com.voxever.teammies.dto.CreateLeagueResponse;
import com.voxever.teammies.dto.LeagueResponse;
import com.voxever.teammies.dto.UpdateLeagueRequest;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public ResponseEntity<CreateLeagueResponse> createLeague(CreateLeagueRequest request, User user) {
        return null;
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
        List<LeagueResponse> leagues = leagueRepository.findAllByUserId(user.getUserId()).stream()
                .map(this::toLeagueResponse)
                .toList();

        return ResponseEntity.ok().body(leagues);
    }

    private LeagueResponse toLeagueResponse(League league) {
        return LeagueResponse.builder()
                .leagueId(league.getLeagueId())
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
