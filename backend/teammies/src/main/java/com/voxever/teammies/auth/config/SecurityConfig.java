package com.voxever.teammies.auth.config;

import com.voxever.teammies.auth.config.filter.JwtFilter;
import com.voxever.teammies.auth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtFilter jwtFilter;
    private static final String[] ALLOWED_PATHS = {
            "/api/auth/**",
            "/error",
            "/api/auth/register",
            "/api/leagues/",
            "/api/leagues/*",
            "/api/leagues/*/quizzes",
            "/api/leagues/*/quizzes/join",
            "/api/leagues/*/ranking",
            "/api/quiz-sessions/*/teams",
            "/api/quiz-sessions/teams/*",
            "/api/quiz-sessions/*/teams/*/members",
            "/api/quiz-sessions/*/teams/join",
            "/api/quiz-sessions/*",
            "/ws/**",
            "/ws-quiz"
    };

    public SecurityConfig(UserRepository userRepository, JwtFilter jwtFilter) {
        this.userRepository = userRepository;
        this.jwtFilter = jwtFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors().and()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(ALLOWED_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new AuthEntryPointJwt())
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement((session)
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(BCryptPasswordEncoder bcrypt) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bcrypt);
        provider.setUserDetailsService(userDetailsService());
        return provider;
    }

    public UserDetailsService userDetailsService() {
        return input -> userRepository.findByEmail(input)
                .or(() -> userRepository.findByUsername(input))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // TODO:: remove after switching db - ignoring h2 path
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
    }

}