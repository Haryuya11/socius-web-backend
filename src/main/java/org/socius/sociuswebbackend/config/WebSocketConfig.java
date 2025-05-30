package org.socius.sociuswebbackend.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    final private ConfigService configService;
    final private OnlineUserService onlineUserService;

    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setThreadNamePrefix("websocket-heartbeat-thread-");
        taskScheduler.setDaemon(true);
        return taskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-heartbeat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            @NonNull ServerHttpRequest request,
                            @NonNull ServerHttpResponse response,
                            @NonNull WebSocketHandler wsHandler,
                            @NonNull Map<String, Object> attributes) throws Exception {

                        boolean result = super.beforeHandshake(request, response, wsHandler, attributes);

                        if (result && request instanceof ServletServerHttpRequest servletRequest) {
                            HttpSession session = servletRequest.getServletRequest().getSession(false);

                            if (session != null) {
                                // Lấy thông tin user từ security context
                                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                                if (auth != null && auth.isAuthenticated()) {
                                    String email = auth.getName();
                                    // Có thể lưu thêm thông tin cần thiết vào attributes
                                    attributes.put("userEmail", email);
                                    attributes.put("sessionId", session.getId());
                                    logger.debug("WebSocket handshake successful for user: {}", email);
                                }
                            }
                        }

                        return result;
                    }
                })
                .withSockJS()
                .setHeartbeatTime(30000); // 30s heartbeat cho SockJS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các endpoint gửi tin nhắn từ client tới server
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

        // Prefix cho các channel mà client có thể subscribe
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{
                        configService.getInt("websocket.heartbeat.send", 25000),
                        configService.getInt("websocket.heartbeat.receive", 25000)
                })
                .setTaskScheduler(webSocketTaskScheduler());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Tăng thời gian timeout cho các kết nối WebSocket
        registration.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024)
                .setTimeToFirstMessage(configService.getInt("websocket.time.to.first.message", 60000));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String sessionId = accessor.getSessionId();
                    String userId = accessor.getFirstNativeHeader("userId");

                    if (userId != null) {
                        Objects.requireNonNull(accessor.getSessionAttributes()).put("userId", userId);
                        logger.debug("User {} connected with session {}", userId, sessionId);
                    }
                }

                return message;
            }
        });
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        logger.info("Handling WebSocket SessionConnectedEvent");

        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            logger.debug("HeaderAccessor created: {}", true);

            if (headerAccessor.getSessionAttributes() != null) {
                logger.debug("Session attributes: {}", headerAccessor.getSessionAttributes().keySet());

                UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
                String sessionId = (String) headerAccessor.getSessionAttributes().get("sessionId");

                logger.debug("Extracted userId: {}, sessionId: {}", userId, sessionId);

                if (userId != null && sessionId != null) {
                    onlineUserService.updateUserOnlineStatus(userId, sessionId);
                    logger.info("Updated online status for user: {}", userId);
                } else {
                    logger.warn("Missing userId or sessionId in session attributes");
                }
            } else {
                logger.warn("Missing headerAccessor or session attributes");
            }

        } catch (Exception e) {
            logger.error("Error in handleSessionConnected: ", e);
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");

        if (userId != null) {
            try {
                UUID userUUID = UUID.fromString(userId);
                OnlineUserService onlineUserService = ApplicationContextHelper.getBean(OnlineUserService.class);
                onlineUserService.markUserOffline(userUUID, sessionId);
                logger.info("User {} disconnected and marked offline", userId);
            } catch (Exception e) {
                logger.error("Error handling session disconnected for user: {}", userId, e);
            }
        }
    }
}
