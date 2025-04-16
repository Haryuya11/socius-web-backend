package org.socius.sociuswebbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Configure WebSocket message broker
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // Thay đổi tùy theo domain của frontend
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các endpoint gửi tin nhắn tới server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho các channel mà client có thể subscribe
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho các tin nhắn private
        registry.setUserDestinationPrefix("/user");
    }
}
