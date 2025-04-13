package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.socius.sociuswebbackend.model.enums.VoteType;

@Entity
@Table(name = "peer_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"voter", "votedEmployee", "period"})
public class PeerVoteEntity extends BaseEntity {

    @NotNull(message = "Voter must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private UserEntity voter;

    @NotNull(message = "Voted employee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voted_employee_id", nullable = false)
    private UserEntity votedEmployee;

    @NotNull(message = "Period must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PeriodEntity period;

    @Column(name = "reason")
    private String reason;

    @NotNull(message = "Vote type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10)
    private VoteType voteType;
}
