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
public class RolePermissionId implements Serializable {
    private UUID roleId;
    private UUID permissionId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RolePermissionId that = (RolePermissionId) o;
        return getRoleId() != null && Objects.equals(getRoleId(), that.getRoleId())
                && getPermissionId() != null && Objects.equals(getPermissionId(), that.getPermissionId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
