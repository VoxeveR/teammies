package com.voxever.teammies.auth.service;

import com.voxever.teammies.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;
    private final Integer jwtTokenValidityMs;

    public JwtService(@Value("${jwt.secretKey}") String secretKey, @Value("90000") Integer jwtTokenValidityMs) {
        this.secretKey = secretKey;
        this.jwtTokenValidityMs = jwtTokenValidityMs;
    }

    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        Instant now = Instant.now();
        return Jwts.builder()
                .addClaims(claims)
                .setSubject(String.valueOf(userId))
                .setHeaderParam("typ", "JWT")
                .setExpiration(Date.from(now.plusMillis(jwtTokenValidityMs)))
                .setIssuedAt(Date.from(now))
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final Long userId = extractUserId(token);

        if (userDetails instanceof User user) {
            return (userId.equals(user.getUserId()) && !isTokenExpired(token));
        }

        return false;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}