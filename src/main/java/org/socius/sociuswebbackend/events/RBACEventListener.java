package org.socius.sociuswebbackend.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.enums.InvalidationReason;
import org.socius.sociuswebbackend.model.messages.SessionInvalidationMessage;
import org.socius.sociuswebbackend.services.MessageProducerService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RBACEventListener {
    private final static Logger logger = LoggerFactory.getLogger(RBACEventListener.class);

    @Autowired
    private MessageProducerService messageProducerService;

    @EventListener
    public void handleRBACEvent(RBACEvent event) {
        logger.info("Nhận được sự kiện thay đổi RBAC: {} cho roleId: {}", event.getType(), event.getRoleId());

        InvalidationReason reason;
        String message = "Quyền hạn đã thay đổi, vui lòng đăng nhập lại để cập nhật quyền hạn mới.";

        switch (event.getType()) {
            case ROLE_UPDATED:
                reason = InvalidationReason.ROLE_CHANGED;
                message = "Vai trò của bạn đã được cập nhật. Vui lòng đăng nhập lại.";
                break;
            case ROLE_DELETED:
                reason = InvalidationReason.ROLE_CHANGED;
                message = "Vai trò của bạn đã bị xóa. Vui lòng đăng nhập lại.";
                break;
            case PERMISSION_UPDATED:
            case PERMISSION_DELETED:
                reason = InvalidationReason.PERMISSION_CHANGED;
                break;
            default:
                reason = InvalidationReason.SECURITY_BREACH;
        }

        messageProducerService.sendSessionInvalidationMessage(
                event.getRoleId(),
                reason,
                message
        );
    }
}
