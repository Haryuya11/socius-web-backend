package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"rolePermissions"})
public class PermissionEntity extends BaseEntity {

    @NotBlank(message = "Permission name must not be empty")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<RolePermissionEntity> rolePermissions = new HashSet<>();
}
