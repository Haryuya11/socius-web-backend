package org.socius.sociuswebbackend.config;

import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.websocket.UserOnlineWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserOnlineWebSocketHandler userOnlineWebSocketHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Lấy danh sách các origin được phép từ ConfigService
        String[] allowedOrigins = configService.getList("cors.allowed.origins")
                .toArray(new String[0]);

        // Điểm kết nối chung
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();

        // Điểm kết nối riêng cho heartbeat
        registry.addEndpoint("/ws-heartbeat")
                .setAllowedOrigins(allowedOrigins)
                .addInterceptors(userOnlineWebSocketHandler)
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

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Tăng thời gian timeout cho các kết nối WebSocket
        registration.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024);
    }
}
