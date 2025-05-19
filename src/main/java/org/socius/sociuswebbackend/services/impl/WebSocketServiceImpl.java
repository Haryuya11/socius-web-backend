package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.TypingIndicatorDto;
import org.socius.sociuswebbackend.services.*;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    final private SimpMessagingTemplate messagingTemplate;

    final private OnlineUserService onlineUserService;

    final private RedisTemplate<String, Object> redisTemplate;

    final private ConfigService configService;

    final private PendingMessagesService pendingMessagesService;

    final private OfflineMessageService offlineMessageService;


    @Override
    public void sendUserLoginNotification(String username) {
        sendNotification("USER_LOGIN", Map.of("username", username));
    }

    @Override
    public void sendUserLogoutNotification(String username) {
        sendNotification("USER_LOGOUT", Map.of("username", username));
    }


    @Override
    public void sendSessionInvalidationNotification(String sessionId, String reason, String message) {
        sendUserSpecificNotification(sessionId, "SESSION_INVALIDATION", Map.of(
                "reason", reason,
                "message", message
        ));
    }

    @Override
    public void handleHeartbeat(UUID userId) {
        try {
            onlineUserService.handleUserHeartbeat(userId);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
        }
    }

    @Override
    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        // Lấy thông tin phiên làm việc từ sự kiện
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (headerAccessor.getSessionAttributes() == null) {
            logger.warn("Không tìm thấy thông tin phiên làm việc trong header");
            return;
        }

        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null && sessionId != null) {
            // Đánh dấu người dùng là offline sau 1 thời gian nhất định
            String disconnectKey = RedisKeyBuilder.wsDisconnectTimeKey(userId);
            redisTemplate.opsForValue().set(
                    disconnectKey,
                    System.currentTimeMillis(),
                    Duration.ofSeconds(configService.getInt("websocket.disconnect.grace.seconds", 30))
            );


            // Tạo chiến lược kết nối lại
            createReconnectStrategy(userId, sessionId);

            // Lưu các tin nhắn chưa được trong thời gian mất kết nối
            pendingMessagesService.initializeBuffer(userId);

            // Thông báo cho những người dùng khác về việc người này mất kết nối
            publishUserStatusChange(userId, "USER_OFFLINE");

            logger.info("Người dùng {} đã ngắt kết nối với websocket", userId);
        }
    }

    @Override
    public void sendTypingIndicator(TypingIndicatorDto typingDto) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + typingDto.getConversationId() + "/typing",
                typingDto
        );
    }

    @Override
    public void handleWebSocketConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (headerAccessor.getSessionAttributes() == null) {
            logger.warn("Không tìm thấy thông tin phiên làm việc trong header");
            return;
        }

        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {

            // Kiểm tra tính hợp lệ của phiên làm việc
            if (isValidSession(sessionId)) {
                logger.warn("Phiên làm việc không hợp lệ: {}", sessionId);
                sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED", "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
                return;
            }


            // Kiểm tra nếu đây là kết nối lại trong thời gian cho phép
            String reconnectKey = RedisKeyBuilder.wsUserReconnectKey(userId, sessionId);
            if (redisTemplate.hasKey(reconnectKey)) {
                // Xử lý kết nối lại
                handleReconnect(userId);
                redisTemplate.delete(reconnectKey);
                logger.info("Xử lý kết nối lại cho người dùng: {}", userId);
            }
            // Đánh dấu người dùng là online
            onlineUserService.updateUserOnlineStatus(userId, sessionId);

            // Gửi lại các tin nhắn offline nếu có
            sendOfflineMessages(userId);

            logger.info("Người dùng {} đã kết nối với websocket", userId);
        }
    }

    @Override
    public boolean validateSessionAndNotify(String sessionId) {
        if (isValidSession(sessionId)) {
            logger.warn("Phiên làm việc không hợp lệ: {}", sessionId);
            sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED", "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
            return false;
        }
        return true;
    }

    /**
     * Tạo xử lý khi người dùng kết nối lại
     *
     * @param userId ID của người dùng
     */
    private void handleReconnect(UUID userId) {
        // Xử lý các tin nhắn đã bị mất trong thời gian mất kết nối
        List<MessageResponseDto> pendingMessages = pendingMessagesService.getPendingMessages(userId);
        if (pendingMessages != null && !pendingMessages.isEmpty()) {
            for (MessageResponseDto message : pendingMessages) {
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/messages",
                        message
                );

                // Giả lập độ trễ giữa các tin nhắn để tránh quá tải
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Xóa buffer sau khi đã gửi lại tin nhắn
            pendingMessagesService.clearBuffer(userId);
            logger.info("Đã gửi lại {} tin nhắn cho người dùng {}", pendingMessages.size(), userId);
        }

        // Thông báo cho người dùng khác rằng người này đã kết nối lại
        publishUserStatusChange(userId, "USER_ONLINE");
    }

    /**
     * Tạo chiến lược kết nối lại cho WebSocket
     *
     * @param userId    ID của người dùng
     * @param sessionId ID của phiên làm việc
     */
    private void createReconnectStrategy(UUID userId, String sessionId) {
        // Lưu vào redis với TTL
        String reconnectKey = RedisKeyBuilder.wsUserReconnectKey(userId, sessionId);
        redisTemplate.opsForValue().set(reconnectKey, System.currentTimeMillis(),
                Duration.ofMinutes(configService.getInt("websocket.reconnect.expiry.minutes", 30)));

        logger.info("Đã tạo chiến lược kết nối lại cho người dùng {} với sessionId {}", userId, sessionId);
    }

    private void sendOfflineMessages(UUID userId) {
        try {
            List<MessageResponseDto> offlineMessages = offlineMessageService.getOfflineMessages(userId);
            if (!offlineMessages.isEmpty()) {
                logger.info("Đang gửi {} tin nhắn offline cho người dùng {}", offlineMessages.size(), userId);

                offlineMessages.sort(Comparator.comparing(MessageResponseDto::getCreatedAt));

                for (MessageResponseDto message : offlineMessages) {
                    messagingTemplate.convertAndSendToUser(
                            userId.toString(),
                            "/queue/offline-messages",
                            message
                    );

                    Thread.sleep(50); // Giả lập độ trễ giữa các tin nhắn để tránh quá tải
                }

                // Xóa tin nhắn offline sau khi đã gửi
                offlineMessageService.clearOfflineMessages(userId);
                logger.info("Đã xóa tin nhắn offline cho người dùng {}", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn offline: {}", e.getMessage(), e);
        }
    }

    private void publishUserStatusChange(UUID userId, String status) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", status);
        notification.put("userId", userId);
        notification.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/user-status", notification);
    }

    /**
     * Gửi thông báo chung cho tất cả người dùng
     */
    private void sendNotification(String type, Map<String, Object> data) {
        try {
            Map<String, Object> notification = new HashMap<>(data);
            notification.put("type", type);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            logger.info("Đã gửi thông báo loại {}: {}", type, data);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo {}: {}", type, e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo riêng cho một người dùng cụ thể
     */
    private void sendUserSpecificNotification(String userId, String type, Map<String, Object> data) {
        try {
            Map<String, Object> notification = new HashMap<>(data);
            notification.put("type", type);
            notification.put("timestamp", System.currentTimeMillis());

            // Gửi cho người dùng cụ thể
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/topic/notifications",
                    notification
            );
            logger.info("Đã gửi thông báo {} cho người dùng {}: {}", type, userId, data);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo {}: {}", type, e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra tính hợp lệ của phiên làm việc
     *
     * @param sessionId ID của phiên làm việc
     * @return true nếu phiên hợp lệ, false nếu đã hết hạn
     */
    private boolean isValidSession(String sessionId) {
        // Kiểm tra trong Redis xem phiên còn tồn tại không
        return redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(sessionId));
    }
}
