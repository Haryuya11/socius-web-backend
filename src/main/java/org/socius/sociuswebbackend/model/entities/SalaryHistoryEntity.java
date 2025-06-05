package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"user"})
public class SalaryHistoryEntity extends BaseEntity {

    @NotNull(message = "User must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull(message = "Previous salary must not be null")
    @Column(name = "previous_salary", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Previous salary cannot be negative")
    private BigDecimal previousSalary;

    @NotNull(message = "New salary must not be null")
    @Column(name = "new_salary", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "New salary cannot be negative")
    private BigDecimal newSalary;

    @NotNull(message = "Effective date must not be null")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reason")
    private String reason;
}
