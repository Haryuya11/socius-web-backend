package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class MessageStatusId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID messageId;
    private UUID userId;

}
