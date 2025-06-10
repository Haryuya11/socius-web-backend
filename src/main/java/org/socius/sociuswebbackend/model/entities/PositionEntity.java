package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"employmentDetails"})
public class PositionEntity extends SoftDeletableEntity {

    @NotBlank(message = "Position name must not be empty")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentDetailEntity> employmentDetails = new HashSet<>();

    public boolean hasActiveEmployees() {
        return employmentDetails.stream()
                .anyMatch(emp -> emp.getWorkingStatus() == WorkingStatus.active);
    }
}
