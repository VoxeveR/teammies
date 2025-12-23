package com.voxever.teammies.auth.service;

import com.voxever.teammies.auth.dto.JwtResponseDto;
import com.voxever.teammies.auth.dto.LoginRequestDto;
import com.voxever.teammies.auth.dto.RegisterRequestDto;
import com.voxever.teammies.auth.dto.RegisterResponseDto;
import com.voxever.teammies.auth.exception.CredentialsIsAlreadyTakenException;
import com.voxever.teammies.auth.repository.UserRepository;
import com.voxever.teammies.entity.RefreshToken;
import com.voxever.teammies.entity.Role;
import com.voxever.teammies.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepo, AuthenticationManager authManager,
                       BCryptPasswordEncoder bCryptPasswordEncoder, JwtService jwt,
                       RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.encoder = bCryptPasswordEncoder;
        this.authManager = authManager;
        this.jwtService = jwt;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public ResponseEntity<RegisterResponseDto> register(RegisterRequestDto registerDto) {
        String newUserEmail = registerDto.getEmail();
        String newUserUsername = registerDto.getUsername();

        checkIfUserDataAlreadyTaken(newUserUsername, newUserEmail);
        User newUser = createUser(registerDto);
        addDefaultRole(newUser);
        userRepo.save(newUser);

        return ResponseEntity.ok()
                .body(RegisterResponseDto.builder()
                        .email(newUserEmail)
                        .username(newUserUsername)
                        .status("OK")
                        .build());
    }

    private void checkIfUserDataAlreadyTaken(String username, String email) throws CredentialsIsAlreadyTakenException {
        if(userRepo.findByUsername(username).isPresent())
            throw new CredentialsIsAlreadyTakenException("Username is taken!");

        if(userRepo.findByEmail(email).isPresent())
            throw new CredentialsIsAlreadyTakenException("Email is already in use!");
    }

    private User createUser(RegisterRequestDto registerDto){
        User createdUser = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(encoder.encode(registerDto.getPassword()))
                .isEnabled(false)
                .build();

        return createdUser;
    }

    private void addDefaultRole(User user){
        Role userRole = Role.builder()
                .role(DEFAULT_ROLE)
                .user(user)
                .build();

        user.setRoles(List.of(userRole));
    }

    public ResponseEntity<JwtResponseDto> authenticate(LoginRequestDto request, HttpServletResponse response) {
        String email = request.getEmail();
        String password = request.getPassword();

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        Optional<User> user = userRepo.findByEmail(email);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User authenticatedUser = user.get();
        String jwtToken = jwtService.generateToken(authenticatedUser.getUserId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);
        String username = authenticatedUser.getUsername();

        long maxAgeSeconds = (refreshToken.getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000;

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getRawToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok()
                .body(JwtResponseDto.builder()
                        .accessToken(jwtToken)
                        .username(username)
                        .accessTokenType("Bearer")
                        .accessTokenExpiresIn(jwtService.extractExpiration(jwtToken).getTime())
                        .build());
    }

    public ResponseEntity<JwtResponseDto> refreshToken(HttpServletRequest servletRequest, HttpServletResponse response) {

        Optional<Cookie> refreshTokenCookie = findRefreshTokenCookie(servletRequest.getCookies());

        if (refreshTokenCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenCookie.get().getValue());
        String jwtToken = jwtService.generateToken(newRefreshToken.getUser().getUserId());

        long maxAgeSeconds = (newRefreshToken.getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000;

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken.getRawToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok()
                .body(JwtResponseDto.builder()
                        .accessToken(jwtToken)
                        .accessTokenType("Bearer")
                        .accessTokenExpiresIn(jwtService.extractExpiration(jwtToken).getTime())
                        .build());
    }


    public ResponseEntity<Void> revokeToken(HttpServletRequest servletRequest) {

        Optional<Cookie> refreshTokenCookie = findRefreshTokenCookie(servletRequest.getCookies());

        if (refreshTokenCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        refreshTokenService.findAndDelete(refreshTokenCookie.get().getValue());

        return ResponseEntity.ok().build();
    }

    private Optional<Cookie> findRefreshTokenCookie(Cookie[] cookies){
        return Arrays.stream(Optional.ofNullable(cookies)
                .orElse(new Cookie[0])).filter(cookie -> cookie.getName().equals("refreshToken")).findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Transactional
    public User registerOAuthUser(String email, String username, String signUpMethod) {

        User newUser = User.builder()
                .email(email)
                .username(username)
                .password(null)
                .isEnabled(true)
                .build();

        Role userRole = Role.builder()
                .role("USER")
                .user(newUser)
                .build();

        newUser.setRoles(List.of(userRole));
        return userRepo.save(newUser);
    }


}
