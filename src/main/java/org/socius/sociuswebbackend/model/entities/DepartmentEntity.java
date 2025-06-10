package org.socius.sociuswebbackend.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"employmentDetail"})
public class DepartmentEntity extends SoftDeletableEntity {

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "group_chat_id")
    private UUID groupChatId;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentDetailEntity> employmentDetail = new HashSet<>();

    public boolean hasActiveEmployees() {
        return employmentDetail.stream()
                .anyMatch(emp -> emp.getWorkingStatus() == WorkingStatus.active);
    }
}