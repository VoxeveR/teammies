package com.voxever.teammies.service;

import com.voxever.teammies.dto.league.*;
import com.voxever.teammies.entity.League;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import com.voxever.teammies.repository.LeagueStandingRepository;
import com.voxever.teammies.repository.QuizRepository;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final QuizRepository quizRepository;
    private final LeagueStandingRepository leagueStandingRepository;

    public LeagueService(LeagueRepository leagueRepository, QuizRepository quizRepository, LeagueStandingRepository leagueStandingRepository) {
        this.leagueRepository = leagueRepository;
        this.quizRepository = quizRepository;
        this.leagueStandingRepository = leagueStandingRepository;
    }

    @Transactional
    public ResponseEntity<CreateLeagueResponse> createLeague(CreateLeagueRequest request, User user) {

        if (leagueRepository.existsByLeagueName(request.getLeagueName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "League with that name already exists");
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end_date must be after start_date");
        }

        League createdLeague = League.builder()
                .leagueName(request.getLeagueName())
                .description(request.getDescription())
                .teamSize(request.getTeamSize())
                .maxTeams(request.getMaxTeams())
                .isPublic(request.getIsPublic())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(user)
                .build();

        leagueRepository.save(createdLeague);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateLeagueResponse.builder()
                        .leagueId(createdLeague.getId())
                        .build());
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

        if (request.getStartDate() != null && request.getEndDate() != null
                && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end_date must be after start_date");
        }

        if (request.getLeagueName() != null) league.setLeagueName(request.getLeagueName());
        if (request.getDescription() != null) league.setDescription(request.getDescription());
        if (request.getTeamSize() != null) league.setTeamSize(request.getTeamSize());
        if (request.getMaxTeams() != null) league.setMaxTeams(request.getMaxTeams());
        if (request.getIsPublic() != null) league.setIsPublic(request.getIsPublic());
        if (request.getStartDate() != null) league.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) league.setEndDate(request.getEndDate());

        leagueRepository.save(league);

        return ResponseEntity.ok(toLeagueResponse(league));
    }

    public ResponseEntity<AllLeaguesResponse> getLeaguesOwnedByUser(User user) {
        List<LeagueResponse> myLeagues = user != null 
                ? leagueRepository.findAllByUser(user).stream()
                    .map(this::toLeagueResponse)
                    .toList()
                : List.of();

        List<LeagueResponse> publicLeagues = leagueRepository.findAllByIsPublicTrue().stream()
                .map(this::toLeagueResponse)
                .toList();

        AllLeaguesResponse response = AllLeaguesResponse.builder()
                .myLeagues(myLeagues)
                .publicLeagues(publicLeagues)
                .build();

        return ResponseEntity.ok().body(response);
    }

    private LeagueResponse toLeagueResponse(League league) {
        return LeagueResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getLeagueName())
                .description(league.getDescription())
                .teamSize(league.getTeamSize())
                .maxTeams(league.getMaxTeams())
                .isPublic(league.getIsPublic())
                .startDate(league.getStartDate())
                .endDate(league.getEndDate())
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

        quizRepository.deleteAll(league.getQuizzes());

        leagueRepository.delete(league);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<LeagueResponse> getLeagueById(Long id, User user) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));

        // Allow access if user is owner OR league is public OR user is not logged in (for public leagues)
        if (user != null && !league.getUser().getUserId().equals(user.getUserId()) && !league.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this league");
        }
        
        // If user is null and league is private, deny access
        if (user == null && !league.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this league");
        }

        return ResponseEntity.ok(toLeagueResponse(league));
    }

    public ResponseEntity<List<LeagueStandingResponseDto>> getLeagueRanking(Long leagueId, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));

        // Allow access if user is owner OR league is public OR user is not logged in (for public leagues)
        if (user != null && !league.getUser().getUserId().equals(user.getUserId()) && !league.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this league's ranking");
        }
        
        // If user is null and league is private, deny access
        if (user == null && !league.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this league's ranking");
        }

        List<LeagueStandingResponseDto> ranking = leagueStandingRepository
                .findAllByLeagueOrderByPointsDescMatchesPlayedAsc(league)
                .stream()
                .map(ls -> LeagueStandingResponseDto.builder()
                        .teamName(ls.getTeam().getName())
                        .points(ls.getPoints())
                        .matchesPlayed(ls.getMatchesPlayed())
                        .build())
                .toList();

        return ResponseEntity.ok(ranking);
    }
}
