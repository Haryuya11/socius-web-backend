package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.RankingCriteria;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"employee", "period"})
public class EmployeeRankingEntity extends BaseEntity {

    @NotNull(message = "Employee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserEntity employee;

    @NotNull(message = "Period must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PeriodEntity period;

    @NotNull(message = "Rank must not be null")
    @Column(name = "rank", nullable = false, precision = 2, scale = 1)
    @DecimalMin(value = "0.0", message = "Rank must be at least 0")
    @DecimalMax(value = "10.0", message = "Rank must be at most 10")
    private BigDecimal rank;

    @NotNull(message = "Criteria must not be null")
    @Column(name = "criteria", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RankingCriteria criteria;
}
