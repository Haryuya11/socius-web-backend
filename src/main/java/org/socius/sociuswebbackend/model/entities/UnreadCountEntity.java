package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unread_counts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversation", "user", "lastReadMessage"})
@EqualsAndHashCode(of = "id")
public class UnreadCountEntity {

    @EmbeddedId
    private UnreadCountId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "unread_count")
    @Builder.Default
    private int unreadCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private MessageEntity lastReadMessage;
}
