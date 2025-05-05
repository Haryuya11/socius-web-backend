package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"employmentDetails", "employmentHistories"})
public class DepartmentEntity extends BaseEntity {

    @NotBlank(message = "Department name must not be empty")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentDetailEntity> employmentDetails = new HashSet<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentHistoryEntity> employmentHistories = new HashSet<>();
}
