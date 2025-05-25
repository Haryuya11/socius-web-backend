package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.socius.sociuswebbackend.model.enums.MemberRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversation", "user"})
@EqualsAndHashCode(of = "id")
public class ConversationMemberEntity {
    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;
}
