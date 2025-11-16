package com.voxever.teammies.auth.service;

import com.voxever.teammies.auth.exception.UserNotFoundException;
import com.voxever.teammies.auth.repository.UserRepository;
import com.voxever.teammies.auth.util.JwtUtils;
import com.voxever.teammies.entity.User;
import org.springframework.stereotype.Service;


@Service
public class AuthContextService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthContextService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public Long extractUserIdFromToken(String rawToken) {
        String cleaned = JwtUtils.cleanToken(rawToken);
        return jwtService.extractUserId(cleaned);
    }

    public User getUserFromToken(String rawToken) {
        Long userId = extractUserIdFromToken(rawToken);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
