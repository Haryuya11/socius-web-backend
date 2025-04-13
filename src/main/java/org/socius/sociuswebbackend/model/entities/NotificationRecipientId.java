package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientId implements Serializable {
    private UUID notificationId;
    private UUID userId;
}
