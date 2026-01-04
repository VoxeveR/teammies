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

import com.voxever.teammies.entity.QuizSession;
import com.voxever.teammies.entity.User;
import com.voxever.teammies.repository.QuizSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    private final QuizSessionRepository quizSessionRepository;
    private static final Pattern ADMIN_EVENTS_PATTERN = 
            Pattern.compile("/topic/quiz-session/([^/]+)/admin/events");

    public WebSocketAuthorizationInterceptor(QuizSessionRepository quizSessionRepository) {
        this.quizSessionRepository = quizSessionRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination != null && destination.contains("/admin/events")) {
                authorizeAdminEventsSubscription(destination);
            }
        }

        return message;
    }

    private void authorizeAdminEventsSubscription(String destination) {

        Matcher matcher = ADMIN_EVENTS_PATTERN.matcher(destination);
        if (!matcher.find()) {
            log.error("Invalid admin/events destination format: {}", destination);
            throw new RuntimeException("Invalid subscription destination");
        }

        String sessionJoinCode = matcher.group(1);
        

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthorized subscription attempt to admin/events: {}", destination);
            throw new RuntimeException("User must be authenticated to access admin events");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) {
            log.warn("Invalid principal type for admin/events subscription");
            throw new RuntimeException("Invalid authentication principal");
        }

        User currentUser = (User) principal;


        QuizSession session = quizSessionRepository.findByJoinCode(sessionJoinCode)
                .orElseThrow(() -> new RuntimeException("Quiz session not found: " + sessionJoinCode));

        Long sessionOrganizerId = session.getQuiz().getCreatedBy().getUserId();
        if (!sessionOrganizerId.equals(currentUser.getUserId())) {
            log.warn("User {} attempted to access admin/events for session {} (organizer: {})",
                    currentUser.getUserId(), sessionJoinCode, sessionOrganizerId);
            throw new RuntimeException("Only quiz organizer can access admin events channel");
        }

        log.info("User {} authorized for admin/events subscription: {}", currentUser.getUserId(), destination);
    }
}
