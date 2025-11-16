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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public ResponseEntity<JwtResponseDto> authenticate(LoginRequestDto request) {
        String email = request.getEmail();
        String password = request.getPassword();

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        String jwtToken = jwtService.generateToken(userRepo.findByEmail(email).get().getUserId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);
        Optional<User> user = userRepo.findByEmail(email);
        String username = "";
        if(user.isPresent()){
            username = user.get().getUsername();
        }
        return ResponseEntity.ok()
                .body(JwtResponseDto.builder()
                        .accessToken(jwtToken)
                        .username(username)
                        .accessTokenType("Bearer")
                        .accessTokenExpiresIn(jwtService.extractExpiration(jwtToken).getTime())
                        .refreshToken(refreshToken.getRawToken())
                        .refreshTokenExpiresIn(refreshToken.getExpiresAt().toEpochMilli())
                        .build());
    }

    public ResponseEntity<JwtResponseDto> refreshToken(String token) {
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(token);
        String jwtToken = jwtService.generateToken(newRefreshToken.getUser().getUserId());

        return ResponseEntity.ok()
                .body(JwtResponseDto.builder()
                        .accessToken(jwtToken)
                        .accessTokenType("Bearer")
                        .accessTokenExpiresIn(jwtService.extractExpiration(jwtToken).getTime())
                        .refreshToken(newRefreshToken.getRawToken())
                        .refreshTokenExpiresIn(newRefreshToken.getExpiresAt().toEpochMilli())
                        .build());
    }

    public void revokeToken(String token) {
        refreshTokenService.findAndDelete(token);
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
