package com.voxever.teammies.auth.controller;

import com.voxever.teammies.auth.dto.*;
import com.voxever.teammies.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<JwtResponseDto> login(@RequestBody @Valid LoginRequestDto request, HttpServletResponse response) {
        return authService.authenticate(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> revokeToken(HttpServletRequest request) {
        return authService.revokeToken(request);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtResponseDto> refreshToken(HttpServletRequest servletRequest, HttpServletResponse response) {
        return authService.refreshToken(servletRequest, response);
    }
}
