package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"leader", "employmentDetailEntities", "employmentHistories"})
public class TeamEntity extends BaseEntity {

    @NotBlank(message = "Team name must not be empty")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @OneToOne
    @JoinColumn(name = "leader_id", unique = true)
    private UserEntity leader;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentDetailEntity> employmentDetailEntities = new HashSet<>();

    @OneToMany(mappedBy = "team")
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentHistoryEntity> employmentHistories = new HashSet<>();
}
