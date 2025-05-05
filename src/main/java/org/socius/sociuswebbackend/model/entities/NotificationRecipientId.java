package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
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
}
