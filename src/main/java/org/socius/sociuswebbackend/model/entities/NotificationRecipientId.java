package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipientId implements Serializable {
    private UUID notificationId;
    private UUID userId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        NotificationRecipientId that = (NotificationRecipientId) o;
        return getNotificationId() != null && Objects.equals(getNotificationId(), that.getNotificationId())
                && getUserId() != null && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(notificationId, userId);
    }
}
