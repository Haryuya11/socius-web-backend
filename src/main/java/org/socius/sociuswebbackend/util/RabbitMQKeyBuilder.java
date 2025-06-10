package org.socius.sociuswebbackend.util;

import java.util.UUID;

/**
 * Utility class cung cấp các phương thức để xây dựng tên queue và exchange cho RabbitMQ
 * theo quy ước nhất quán
 */
public class RabbitMQKeyBuilder {
    // Exchange
    public static final String SESSION_MANAGEMENT_EXCHANGE = "session.management";
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "chat.dlx";

    // Queue
    public static final String SESSION_INVALIDATION_QUEUE = "session.invalidation";
    public static final String PRIVATE_MESSAGE_QUEUE = "chat.private.queue";
    public static final String GROUP_MESSAGE_QUEUE = "chat.group.queue";
    public static final String READ_RECEIPT_QUEUE = "chat.receipt.queue";
    public static final String DEAD_LETTER_QUEUE = "chat.dlq";

    // Routing key
    public static final String INVALIDATE_SESSION_ROUTING_KEY = "invalidate.session";
    public static final String PRIVATE_ROUTING_KEY = "chat.message.private.*";
    public static final String GROUP_ROUTING_KEY = "chat.message.group.*";
    public static final String READ_RECEIPT_ROUTING_KEY = "chat.receipt.*";
    public static final String DEAD_LETTER_ROUTING_KEY = "chat.message.failed";

    // Pending messages key
    public static final String PENDING_MESSAGES_KEY = "pending:messages:";

    // Chat Pattern Builder
    public static String getPrivateRoutingKeyPattern(UUID conversationId) {
        return "chat.message.private." + conversationId;
    }

    public static String getGroupRoutingKeyPattern(UUID conversationId) {
        return "chat.message.group." + conversationId;
    }

    public static String getReadReceiptRoutingKeyPattern(UUID conversationId) {
        return "chat.receipt." + conversationId;
    }


    // Exchange getters
    public static String getSessionManagementExchange() {
        return SESSION_MANAGEMENT_EXCHANGE;
    }

    public static String getChatExchange() {
        return CHAT_EXCHANGE;
    }

    public static String getDeadLetterExchange() {
        return DEAD_LETTER_EXCHANGE;
    }

    // Queue getters
    public static String getSessionInvalidationQueue() {
        return SESSION_INVALIDATION_QUEUE;
    }

    public static String getPrivateMessageQueue() {
        return PRIVATE_MESSAGE_QUEUE;
    }

    public static String getGroupMessageQueue() {
        return GROUP_MESSAGE_QUEUE;
    }

    public static String getReadReceiptQueue() {
        return READ_RECEIPT_QUEUE;
    }

    public static String getDeadLetterQueue() {
        return DEAD_LETTER_QUEUE;
    }

    // Routing key getters
    public static String getInvalidateSessionRoutingKey() {
        return INVALIDATE_SESSION_ROUTING_KEY;
    }

    public static String getPrivateRoutingKey() {
        return PRIVATE_ROUTING_KEY;
    }

    public static String getGroupRoutingKey() {
        return GROUP_ROUTING_KEY;
    }

    public static String getReadReceiptRoutingKey() {
        return READ_RECEIPT_ROUTING_KEY;
    }

    public static String getDeadLetterRoutingKey() {
        return DEAD_LETTER_ROUTING_KEY;
    }

    public static String getPendingMessagesKey(UUID userId) {
        return PENDING_MESSAGES_KEY + userId;
    }

    public static String getPendingMessagesPattern() {
        return PENDING_MESSAGES_KEY + "*";
    }
}
