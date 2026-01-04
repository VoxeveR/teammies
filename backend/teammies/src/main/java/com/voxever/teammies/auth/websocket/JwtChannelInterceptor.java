package com.voxever.teammies.auth.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.voxever.teammies.auth.repository.UserRepository;
import com.voxever.teammies.auth.service.JwtService;
import com.voxever.teammies.entity.User;

import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            if (StompCommand.CONNECT.equals(command)) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                String token = null;

                try {
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7).trim();
                    }

                    if (token != null) {
                        Long userId = jwtService.extractUserId(token);
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                        if (jwtService.validateToken(token, user)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            accessor.setUser(authToken);
                            log.info("WebSocket user authenticated: {}", userId);
                        }
                    } else {
                        log.warn("No JWT token provided for WebSocket connection");
                    }
                } catch (SignatureException e) {
                    log.error("Invalid JWT token: {}", e.getMessage());
                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    log.error("JWT token expired: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("Error processing WebSocket authentication: {}", e.getMessage());
                }
            }
        }

        return message;
    }
}
