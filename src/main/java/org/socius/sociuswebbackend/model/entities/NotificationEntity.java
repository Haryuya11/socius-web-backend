package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"sender", "recipients"})
public class NotificationEntity extends BaseEntity {

    @NotBlank(message = "Title must not be empty")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotNull(message = "Sender must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @NotBlank(message = "Message must not be empty")
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull(message = "Expiry date must not be null")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull(message = "Notification type must not be null")
    @Column(name = "type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @NotNull(message = "Is urgent flag must not be null")
    @Column(name = "is_urgent", nullable = false)
    private Boolean isUrgent;

    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<NotificationRecipientEntity> recipients = new HashSet<>();

    @Override
    protected void validateEntity() {
        validateDates();
    }

    private void validateDates() {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }
    }
}
