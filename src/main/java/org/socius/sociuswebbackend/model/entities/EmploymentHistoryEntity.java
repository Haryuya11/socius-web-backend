package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"user", "position", "department", "team", "role"})
public class EmploymentHistoryEntity extends BaseEntity {

    @NotNull(message = "User must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionEntity position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @NotNull(message = "Start date must not be null")
    @Column(name = "start_date", nullable = false)
    @PastOrPresent(message = "Start date must be in the past or present")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Salary must not be null")
    @Column(name = "salary", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Salary must be a positive number")
    private BigDecimal salary;

    @Column(name = "description")
    private String description;

    @Override
    protected void validateEntity() {
        validateDates();
    }

    /**
     * Validate rằng end date phải sau start date
     */
    private void validateDates() {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}