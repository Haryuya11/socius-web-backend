package org.socius.sociuswebbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

@Configuration
public class SessionEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionEventListener.class);

    @EventListener
    public void handleSessionCreated(HttpSessionCreatedEvent event) {
        logger.info("Session created: {}", event.getSession().getId());
    }

    @EventListener
    public void handleSessionDestroyed(HttpSessionDestroyedEvent event) {
        logger.info("Session destroyed: {}", event.getSession().getId());
    }

    @EventListener
    public void handleSessionExpired(SessionDestroyedEvent event) {
        logger.info("Session expired: {}", event.getId());
    }
}
