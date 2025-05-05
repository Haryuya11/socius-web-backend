package org.socius.sociuswebbackend.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.enums.InvalidationReason;
import org.socius.sociuswebbackend.services.MessageProducerService;
import org.socius.sociuswebbackend.services.RBACRedisService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RBACEventListenerTest {
    @Mock
    private RBACRedisService rbacRedisService;

    @Mock
    private MessageProducerService messageProducerService;

    @InjectMocks
    private RBACEventListener rbacEventListener;

    private final UUID roleId = UUID.randomUUID();


    @Test
    @DisplayName("Xử lý sự kiện cập nhật vai trò")
    void handleRoleUpdatedEvent() {
        RBACEvent event = new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED);

        rbacEventListener.handleRBACEvent(event);

        verify(messageProducerService).sendSessionInvalidationMessage(
                eq(roleId), eq(InvalidationReason.ROLE_CHANGED), any(String.class)
        );
    }

    @Test
    @DisplayName("Xử lý sự kiện xóa vai trò")
    void handleRoleDeletedEvent() {
        RBACEvent event = new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_DELETED);

        rbacEventListener.handleRBACEvent(event);

        verify(messageProducerService).sendSessionInvalidationMessage(
                eq(roleId), eq(InvalidationReason.ROLE_CHANGED), any(String.class)
        );
    }

    @Test
    @DisplayName("Xử lý sự kiện cập nhật quyền")
    void handlePermissionUpdatedEvent() {
        RBACEvent event = new RBACEvent(this, roleId, RBACEvent.EventType.PERMISSION_UPDATED);

        rbacEventListener.handleRBACEvent(event);

        verify(messageProducerService).sendSessionInvalidationMessage(
                eq(roleId), eq(InvalidationReason.PERMISSION_CHANGED), any(String.class)
        );
    }

    @Test
    @DisplayName("Xử lý sự kiện xóa quyền")
    void handlePermissionDeletedEvent() {
        RBACEvent event = new RBACEvent(this, roleId, RBACEvent.EventType.PERMISSION_DELETED);

        rbacEventListener.handleRBACEvent(event);

        verify(messageProducerService).sendSessionInvalidationMessage(
                eq(roleId), eq(InvalidationReason.PERMISSION_CHANGED), any(String.class)
        );
    }

}
