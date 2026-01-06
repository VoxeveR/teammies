package com.voxever.teammies.auth.websocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.voxever.teammies.auth.repository.UserRepository;
import com.voxever.teammies.auth.service.JwtService;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.QuizSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    private final QuizSessionRepository quizSessionRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final Pattern ADMIN_EVENTS_PATTERN = 
            Pattern.compile("/topic/quiz-session/([^/]+)/admin/events");

    public WebSocketAuthorizationInterceptor(QuizSessionRepository quizSessionRepository,
                                              JwtService jwtService,
                                              UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination != null && destination.contains("/admin/events")) {
                authorizeAdminEventsSubscription(destination, accessor);
            }
        }

        return message;
    }

    private void authorizeAdminEventsSubscription(String destination, StompHeaderAccessor accessor) {

        Matcher matcher = ADMIN_EVENTS_PATTERN.matcher(destination);
        if (!matcher.find()) {
            log.error("Invalid admin/events destination format: {}", destination);
            throw new RuntimeException("Invalid subscription destination");
        }

        String sessionJoinCode = matcher.group(1);
        
        User currentUser = null;
        
        // First, try to get user from session attributes (stored during CONNECT by JwtChannelInterceptor)
        Object userFromSession = accessor.getSessionAttributes().get("AUTHENTICATED_USER");
        if (userFromSession instanceof User) {
            currentUser = (User) userFromSession;
            log.debug("User retrieved from session attributes for admin/events subscription");
        }
        
        // If not found in session, try accessor.getUser()
        if (currentUser == null) {
            Object userPrincipal = accessor.getUser();
            if (userPrincipal instanceof org.springframework.security.core.userdetails.UserDetails) {
                if (userPrincipal instanceof User) {
                    currentUser = (User) userPrincipal;
                    log.debug("User authenticated from accessor for admin/events subscription");
                }
            }
        }
        
        // If still not found, try SecurityContextHolder
        if (currentUser == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
                currentUser = (User) auth.getPrincipal();
                log.debug("User authenticated from SecurityContextHolder for admin/events subscription");
            }
        }

        if (currentUser == null) {
            log.warn("Unauthorized subscription attempt to admin/events: {}", destination);
            throw new RuntimeException("User must be authenticated to access admin events");
        }

        // Get quiz organizer ID directly without loading the entire entity and its relationships
        Long sessionOrganizerId = quizSessionRepository.findQuizOrganizerIdBySessionJoinCode(sessionJoinCode)
                .orElseThrow(() -> new RuntimeException("Quiz session not found: " + sessionJoinCode));

        if (!sessionOrganizerId.equals(currentUser.getUserId())) {
            log.warn("User {} attempted to access admin/events for session {} (organizer: {})",
                    currentUser.getUserId(), sessionJoinCode, sessionOrganizerId);
            throw new RuntimeException("Only quiz organizer can access admin events channel");
        }

        log.info("User {} authorized for admin/events subscription: {}", currentUser.getUserId(), destination);
    }
}
