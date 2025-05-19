package org.socius.sociuswebbackend.util;

import java.util.UUID;

/**
 * Utility class cung cấp các phương thức để xây dựng key theo quy ước nhất quán
 * Quy ước đặt tên key: prefix:entity:identifier:attribute
 */
public class RedisKeyBuilder {
    // Các prefix chính
    private static final String USER_PREFIX = "user:";
    private static final String SESSION_PREFIX = "session:";
    private static final String ROLE_PREFIX = "role:";
    private static final String WS_PREFIX = "ws:";
    private static final String CHAT_PREFIX = "chat:";
    private static final String ONLINE_PREFIX = "online:";
    private static final String RBAC_PREFIX = "rbac:";
    private static final String CACHE_PREFIX = "cache:";
    private static final String SPRING_SESSION_PREFIX = "spring:session:";
    private static final String RECONNECT_PREFIX = "reconnect:";
    private static final String DISCONNECT_PREFIX = "disconnect:";
    private static final String PENDING_PREFIX = "pending:";
    private static final String FAILED_PREFIX = "failed:";


    // -------------------------------------------------------------------------
    // User related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho trạng thái online của người dùng
     * @param userId ID người dùng
     * @return Chuỗi key theo format: user:{userId}:online
     */
    public static String userOnlineKey(UUID userId) {
        return USER_PREFIX + userId + ":online";
    }

    /**
     * Key cho danh sách session của người dùng
     * @param userId ID người dùng
     * @return Chuỗi key theo format: user:{userId}:sessions
     */
    public static String userSessionsKey(UUID userId) {
        return USER_PREFIX + userId + ":sessions";
    }

    /**
     * Key cho session hiện tại của người dùng
     * @param userId ID người dùng
     * @return Chuỗi key theo format: user:{userId}:sessionid
     */
    public static String userCurrentSessionKey(UUID userId) {
        return USER_PREFIX + userId + ":sessionid";
    }

    /**
     * Key cho heartbeat của người dùng
     * @param userId ID người dùng
     * @return Chuỗi key theo format: user:{userId}:heartbeat
     */
    public static String userHeartbeatKey(UUID userId) {
        return USER_PREFIX + userId + ":heartbeat";
    }

    // -------------------------------------------------------------------------
    // Session related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho thông tin RBAC của một session
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: session:{sessionId}:rbac
     */
    public static String sessionRbacKey(String sessionId) {
        return SESSION_PREFIX + sessionId + ":rbac";
    }

    /**
     * Key cho thông tin user của một session
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: session:{sessionId}:userid
     */
    public static String sessionUserKey(String sessionId) {
        return SESSION_PREFIX + sessionId + ":userid";
    }

    /**
     * Key cho session được Spring quản lý
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: spring:session:sessions:{sessionId}
     */
    public static String springSessionKey(String sessionId) {
        return SPRING_SESSION_PREFIX + "sessions:" + sessionId;
    }

    /**
     * Key cho thời gian hết hạn của session
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: spring:session:sessions:expires:{sessionId}
     */
    public static String springSessionExpiresKey(String sessionId) {
        return SPRING_SESSION_PREFIX + "sessions:expires:" + sessionId;
    }

    // -------------------------------------------------------------------------
    // Role related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho danh sách session liên quan đến một vai trò
     * @param roleId ID vai trò
     * @return Chuỗi key theo format: role:{roleId}:sessions
     */
    public static String roleSessionsKey(UUID roleId) {
        return ROLE_PREFIX + roleId + ":sessions";
    }

    /**
     * Key cho danh sách người dùng thuộc một vai trò
     * @param roleId ID vai trò
     * @return Chuỗi key theo format: role:users:{roleId}
     */
    public static String roleUsersKey(UUID roleId) {
        return ROLE_PREFIX + "users:" + roleId;
    }

    // -------------------------------------------------------------------------
    // WebSocket related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho thông tin kết nối lại WebSocket
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: ws:session:{sessionId}:reconnect
     */
    public static String wsReconnectKey(String sessionId) {
        return WS_PREFIX + "session:" + sessionId + ":reconnect";
    }

    /**
     * Key cho thông tin kết nối lại WebSocket của người dùng cụ thể
     * @param userId ID người dùng
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: reconnect:{userId}:{sessionId}
     */
    public static String wsUserReconnectKey(UUID userId, String sessionId) {
        return RECONNECT_PREFIX + userId + ":" + sessionId;
    }

    /**
     * Key cho thời gian ngắt kết nối
     * @param userId ID người dùng
     * @return Chuỗi key theo format: disconnect:time:{userId}
     */
    public static String wsDisconnectTimeKey(UUID userId) {
        return DISCONNECT_PREFIX + "time:" + userId;
    }

    // -------------------------------------------------------------------------
    // Chat related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho tin nhắn đang chờ gửi
     * @param userId ID người dùng
     * @return Chuỗi key theo format: chat:user:{userId}:pending
     */
    public static String chatPendingKey(UUID userId) {
        return CHAT_PREFIX + "user:" + userId + ":pending";
    }

    /**
     * Key cho tin nhắn offline
     * @param userId ID người dùng
     * @return Chuỗi key theo format: chat:user:{userId}:offline
     */
    public static String chatOfflineKey(UUID userId) {
        return CHAT_PREFIX + "user:" + userId + ":offline";
    }

    /**
     * Key cho thông tin phiên liên kết với tin nhắn chờ
     * @param userId ID người dùng
     * @return Chuỗi key theo format: pending:session:{userId}
     */
    public static String pendingSessionKey(UUID userId) {
        return PENDING_PREFIX + "session:" + userId;
    }

    /**
     * Key cho thông tin receipt thất bại
     * @param userId ID người dùng
     * @param conversationId ID cuộc trò chuyện
     * @return Chuỗi key theo format: chat:receipt:failed:{userId}:{conversationId}
     */
    public static String failedReadReceiptKey(UUID userId, UUID conversationId) {
        return CHAT_PREFIX + "receipt:failed:" + userId + ":" + conversationId;
    }

    // -------------------------------------------------------------------------
    // RBAC related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho thông tin RBAC
     * @param sessionId ID phiên
     * @return Chuỗi key theo format: rbac:{sessionId}
     */
    public static String rbacKey(String sessionId) {
        return RBAC_PREFIX + sessionId;
    }

    // -------------------------------------------------------------------------
    // Cache related keys
    // -------------------------------------------------------------------------

    /**
     * Key cho cache cấu hình
     * @param key Khóa cấu hình
     * @return Chuỗi key theo format: cache:config:{key}
     */
    public static String configCacheKey(String key) {
        return CACHE_PREFIX + "config:" + key;
    }

    /**
     * Tạo pattern để tìm kiếm key theo prefix
     * @param prefix Tiền tố cần tìm
     * @return Pattern dạng prefix*
     */
    public static String getKeyPattern(String prefix) {
        return prefix + "*";
    }
}