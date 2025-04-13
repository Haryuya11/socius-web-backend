package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "position", "department", "team", "role"})
public class EmploymentDetailEntity extends BaseEntity {

    @NotNull(message = "User must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @NotNull(message = "Position must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionEntity position;

    @NotNull(message = "Department must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private TeamEntity team;

    @NotNull(message = "Role must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @NotNull(message = "Start date must not be null")
    @Column(name = "start_date", nullable = false)
    @PastOrPresent(message = "Start date must be in the past or present")
    private LocalDate startDate;

    @NotNull(message = "Salary must not be null")
    @Column(name = "salary", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Salary must be a positive number")
    private BigDecimal salary;

    @NotNull(message = "Working status must not be null")
    @Column(name = "working_status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private WorkingStatus workingStatus;
}
