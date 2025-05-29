package org.socius.sociuswebbackend.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.socius.sociuswebbackend.websocket.UserOnlineWebSocketHandler;
import org.socius.sociuswebbackend.websocket.WebSocketCsrfInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    final private ConfigService configService;
    final private UserOnlineWebSocketHandler userOnlineWebSocketHandler;
    final private RedisTemplate<String, Object> redisTemplate;
    final private WebSocketCsrfInterceptor webSocketCsrfInterceptor;

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

        // Lấy danh sách các origin được phép từ ConfigService
        String[] allowedOrigins = configService.getList("cors.allowed.origins")
                .toArray(new String[0]);

        // Điểm kết nối chung
        registry.addEndpoint("/ws-heartbeat")
                .setAllowedOrigins(allowedOrigins)
                .addInterceptors(webSocketCsrfInterceptor)
                .withSockJS()
                .setSessionCookieNeeded(true)
                .setClientLibraryUrl("//cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");

        // Điểm kết nối riêng cho heartbeat
        registry.addEndpoint("/ws-heartbeat")
                .setAllowedOrigins(allowedOrigins)
                .addInterceptors(userOnlineWebSocketHandler, webSocketCsrfInterceptor)
                .withSockJS();
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

                assert accessor != null;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Xác thực người dùng khi kết nối
                    String sessionId = accessor.getSessionId();

                    // Kiểm tra tính hợp lệ của phiên
                    if (!redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(sessionId))) {
                        // Nếu phiên không hợp lệ, ném ngoại lệ
                        logger.warn("Từ chối kết nối WebSocket do phiên không hợp lệ: {}", sessionId);
                        return null;
                    }

                    accessor.setUser(() -> sessionId);
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    // Xử lý khi client subscribe vào một topic
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/topic/conversations/")) {
                        // Có thể thêm logic xác thực quyền truy cập vào cuộc trò chuyện ở đây
                        logger.info("Người dùng đăng ký nhận tin nhắn từ: {}", destination);
                    }
                }
                return message;
            }
        });
    }
}
