package org.socius.sociuswebbackend.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.util.UUID;

/**
 * Sự kiện RBAC (Role-Based Access Control) để thông báo về các thay đổi liên quan đến vai trò và quyền hạn.
 */
@Getter
@Setter
public class RBACEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID roleId;
    private final EventType type;

    public enum EventType {
        ROLE_UPDATED,
        ROLE_DELETED,
        PERMISSION_UPDATED,
        PERMISSION_DELETED
    }

    public RBACEvent(Object source, UUID roleId, EventType eventType) {
        super(source);
        this.roleId = roleId;
        this.type = eventType;
    }

}

