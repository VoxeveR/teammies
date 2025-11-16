package com.voxever.teammies.controller;

import com.voxever.teammies.auth.dto.RegisterRequestDto;
import com.voxever.teammies.auth.dto.RegisterResponseDto;
import com.voxever.teammies.dto.CreateLeagueRequest;
import com.voxever.teammies.dto.CreateLeagueResponse;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.LeagueRepository;
import com.voxever.teammies.service.LeagueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/league")
@CrossOrigin(origins = "http://localhost:5173")
public class LeagueController {

    private LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping("/add")
    public ResponseEntity<CreateLeagueResponse> createLeague(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken, @RequestBody @Valid CreateLeagueRequest request, @AuthenticationPrincipal User user) {
        return leagueService.createLeague(request, user);
    }

}
