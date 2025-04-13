package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.socius.sociuswebbackend.model.enums.PeriodStatus;
import org.socius.sociuswebbackend.model.enums.PeriodType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"performanceReviews", "peerVotes", "employeeRankings"})
public class PeriodEntity extends BaseEntity {

    @NotBlank(message = "Period name must not be empty")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Period type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private PeriodType type;

    @NotNull(message = "Start date must not be null")
    @PastOrPresent(message = "Start date must be in the past or present")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PeriodStatus status;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "period", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PerformanceReviewEntity> performanceReviews = new HashSet<>();

    @OneToMany(mappedBy = "period", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PeerVoteEntity> peerVotes = new HashSet<>();

    @OneToMany(mappedBy = "period", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmployeeRankingEntity> employeeRankings = new HashSet<>();

    @PrePersist
    @PreUpdate void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
