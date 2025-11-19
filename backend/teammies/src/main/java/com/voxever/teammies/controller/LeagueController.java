package com.voxever.teammies.controller;


import com.voxever.teammies.dto.league.CreateLeagueRequest;
import com.voxever.teammies.dto.league.CreateLeagueResponse;
import com.voxever.teammies.dto.league.LeagueResponse;
import com.voxever.teammies.dto.league.UpdateLeagueRequest;
import com.voxever.teammies.entity.User;

import com.voxever.teammies.service.LeagueService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "http://localhost:5173/leagues")
public class LeagueController {

    private LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping("/")
    public ResponseEntity<List<LeagueResponse>> getMyLeagues(@AuthenticationPrincipal User user) {
        return leagueService.getLeaguesOwnedByUser(user);
    }

    @PostMapping("/")
    public ResponseEntity<CreateLeagueResponse> createLeague(@RequestBody @Valid CreateLeagueRequest request, @AuthenticationPrincipal User user) {
        return leagueService.createLeague(request, user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeagueResponse> updateLeague(
            @PathVariable Long id,
            @RequestBody @Valid UpdateLeagueRequest request,
            @AuthenticationPrincipal User user) {

        return leagueService.updateLeague(id, request, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return leagueService.deleteLeague(id, user);
    }

//    @GetMapping("/all")
//    public ResponseEntity<List<LeagueResponse>> getAllLeagues() {
//        return leagueService.getAllLeagues();
//    }



//    @GetMapping("/{id}")
//    public ResponseEntity<LeagueResponse> getLeagueById(@PathVariable Long id) {
//        return leagueService.getLeagueById(id);
//    }







}
