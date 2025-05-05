package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AppSettingsEntity extends BaseEntity {
    @NotBlank(message = "Setting key must not be empty")
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @NotBlank(message = "Setting value must not be empty")
    @Column(name = "setting_value", nullable = false)
    private String settingValue;

    @Column(name = "description")
    private String description;
}
