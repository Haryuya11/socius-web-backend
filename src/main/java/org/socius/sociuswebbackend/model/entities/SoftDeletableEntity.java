package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.EntityStatus;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SoftDeletableEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft delete the entity
     */
    public void softDelete() {
        this.status = EntityStatus.deleted;
        this.deletedAt = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Restore the soft deleted entity
     */
    public void restore() {
        this.status = EntityStatus.active;
        this.deletedAt = null;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Check if entity is soft deleted
     */
    public boolean isDeleted() {
        return EntityStatus.deleted.equals(this.status);
    }

    /**
     * Check if entity is active
     */
    public boolean isActive() {
        return EntityStatus.active.equals(this.status);
    }
}