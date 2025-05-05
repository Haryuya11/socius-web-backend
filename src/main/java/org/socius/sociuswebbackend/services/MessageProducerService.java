package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.enums.InvalidationReason;

import java.util.Set;
import java.util.UUID;

public interface MessageProducerService {

    /**
     * Gửi thông điệp hủy tất cả phiên của người dùng thuộc một role cụ thể.
     *
     * @param roleID  ID của role
     * @param reason  Lý do hủy phiên
     * @param message Thông điệp bổ sung
     */
    void sendSessionInvalidationMessage(UUID roleID, InvalidationReason reason, String message);

    /**
     * Gửi thông điệp hủy phiên cụ thể cho một hoặc nhiều session cụ thể.
     *
     * @param sessionIds ID của các session cần hủy
     * @param reason     Lý do hủy phiên
     * @param message    Thông điệp bổ sung
     */
    void sendSpecificSessionInvalidationMessage(Set<String> sessionIds, InvalidationReason reason, String message);
}
