package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ConversationMemberId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID conversationId;
    private UUID userId;
}
