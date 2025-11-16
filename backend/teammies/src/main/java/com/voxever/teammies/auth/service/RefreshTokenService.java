package com.voxever.teammies.auth.service;

import com.voxever.teammies.auth.repository.RefreshTokenRepository;
import com.voxever.teammies.auth.repository.UserRepository;
import com.voxever.teammies.entity.RefreshToken;
import com.voxever.teammies.entity.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 3;
    private static final int PREFIX_LENGTH = 8;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo,
                               UserRepository userRepo,
                               BCryptPasswordEncoder encoder) {
        this.refreshTokenRepository = refreshTokenRepo;
        this.userRepository = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        RefreshToken refreshToken = buildToken(user);
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = buildToken(user);
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    private RefreshToken buildToken(User user) {
        String rawToken = generateRawToken();
        String hashedToken = encoder.encode(rawToken);

        return RefreshToken.builder()
                .expiresAt(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS))
                .hashedToken(hashedToken)
                .prefix(rawToken.substring(0, PREFIX_LENGTH))
                .rawToken(rawToken)
                .user(user)
                .build();
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void findAndDelete(String token) {
        RefreshToken removeToken = findByPrefix(token);
        refreshTokenRepository.delete(removeToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String rawToken) {
        RefreshToken oldRefreshToken = findByPrefix(rawToken);
        User tokenOwner = oldRefreshToken.getUser();

        verifyExpirationDate(oldRefreshToken);
        refreshTokenRepository.delete(oldRefreshToken);

        return createRefreshToken(tokenOwner);
    }

    private RefreshToken findByPrefix(String rawToken) {
        String tokenPrefix = rawToken.substring(0, PREFIX_LENGTH);
        List<RefreshToken> tokenCandidates = refreshTokenRepository.findAllByPrefix(tokenPrefix);

        return tokenCandidates.stream()
                .filter(token -> encoder.matches(rawToken, token.getHashedToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token doesn't exist!"));
    }

    private void verifyExpirationDate(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException(token.getHashedToken() + " Refresh token has expired. Login once again");
        }
    }

}