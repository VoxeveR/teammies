package com.voxever.teammies.auth.controller;

import com.voxever.teammies.auth.dto.*;
import com.voxever.teammies.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        return authService.authenticate(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> revokeToken(@RequestBody RefreshTokenRequestDto request) {
        authService.revokeToken(request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        return authService.refreshToken(request.getToken());
    }

}
