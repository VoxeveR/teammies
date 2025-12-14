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


    public SecurityConfig(UserRepository userRepository, JwtFilter jwtFilter) {
        this.userRepository = userRepository;
        this.jwtFilter = jwtFilter;
    }

    //TODO: RESTORE AUTHENTICATION
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors().and()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/leagues/*/quizzes/join").permitAll()
                        .requestMatchers("/api/quiz-sessions/*/teams").permitAll()
                        .requestMatchers("/api/quiz-sessions/teams/*").permitAll()
                        .requestMatchers("/api/quiz-sessions/*/teams/*/members").permitAll()
                        .requestMatchers("/api/quiz-sessions/*/teams/join").permitAll()
                        .requestMatchers("/api/quiz-sessions/*").permitAll()

                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws-quiz").permitAll()
                      //  .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated())
//                .oauth2Login(oauth2 -> {
//                    oauth2.successHandler(oauth2LoginSuccessHandler);
//                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new AuthEntryPointJwt())
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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