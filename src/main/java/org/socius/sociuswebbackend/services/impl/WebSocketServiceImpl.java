package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.TypingIndicatorDto;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    final private SimpMessagingTemplate messagingTemplate;
    final private OnlineUserService onlineUserService;
    final private RedisTemplate<String, Object> redisTemplate;
    final private OfflineMessageService offlineMessageService;
    final private SimpMessagingTemplate simpMessagingTemplate;


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
    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        // Lấy thông tin phiên làm việc từ sự kiện
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (headerAccessor.getSessionAttributes() == null) {
            logger.warn("Không tìm thấy session attributes trong disconnect event");
            return;
        }

        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null && sessionId != null) {
            logger.info("WebSocket disconnect - user: {}, session: {}", userId, sessionId);

            onlineUserService.markUserOffline(userId, sessionId);

            // Thông báo cho những người dùng khác về việc người này mất kết nối
            simpMessagingTemplate.convertAndSend("/topic/user-status",
                    Map.of(
                            "userId", userId,
                            "isOnline", false,
                            "event", "disconnect"
                    ));

            logger.info("Người dùng {} đã ngắt kết nối với websocket", userId);
        }
    }


    @Override
    @EventListener
    public void handleWebSocketConnectEvent(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();

            if (headerAccessor.getSessionAttributes() == null) {
                logger.warn("Không tìm thấy thông tin phiên làm việc trong header");
                return;
            }

            UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
            if (userId != null) {
                // Kiểm tra tính hợp lệ của phiên làm việc
                if (!isValidSession(sessionId)) {
                    logger.warn("Phiên làm việc không hợp lệ: {}", sessionId);
                    sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED", "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
                    return;
                }

                // Đánh dấu người dùng là online
                onlineUserService.updateUserOnlineStatus(userId, sessionId);

                // Gửi lại các tin nhắn offline nếu có
                CompletableFuture.runAsync(() -> {
                    try {
                        sendOfflineMessages(userId);
                    } catch (Exception e) {
                        logger.error("Lỗi khi gửi offline messages cho user {}: {}", userId, e.getMessage(), e);
                    }
                });

                logger.info("Người dùng {} đã kết nối với websocket", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi trong handleWebSocketConnectEvent: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean validateSessionAndNotify(String sessionId) {
        if (!isValidSession(sessionId)) {
            logger.warn("Phiên làm việc không hợp lệ: {}", sessionId);
            sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED", "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
            return false;
        }
        return true;
    }

    @Override
    public void sendTypingIndicator(TypingIndicatorDto typingDto) {
        try {

            if (typingDto.getConversationId() == null || typingDto.getUserId() == null) {
                logger.warn("Không thể gửi thông báo trạng thái gõ: ID cuộc trò chuyện hoặc ID người dùng không hợp lệ");
                return;
            }
            logger.info("Người dùng {} đang gõ trong cuộc trò chuyện {}", typingDto.getUserName(), typingDto.getConversationId());

            Map<String, Object> typingNotification = new HashMap<>();
            typingNotification.put("userId", typingDto.getUserId());
            typingNotification.put("userName", typingDto.getUserName());
            typingNotification.put("typing", typingDto.isTyping());

            // Gửi thông báo trạng thái gõ tới tất cả người dùng trong cuộc trò chuyện
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + typingDto.getConversationId() + "/typing",
                    typingNotification
            );
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo trạng thái gõ: {}", e.getMessage(), e);
        }
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
            notification.put("messageId", UUID.randomUUID().toString());

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
        try {
            String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);
            Boolean exists = redisTemplate.hasKey(sessionKey);
            Long expireTime = redisTemplate.getExpire(sessionKey);

            return exists != null && exists && expireTime != null && expireTime > 0;
        } catch (Exception e) {
            logger.error("Lỗi kiểm tra session validity: {}", e.getMessage(), e);
            return false;
        }
    }
}
