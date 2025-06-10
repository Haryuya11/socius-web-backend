package org.socius.sociuswebbackend.model.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = { "user", "password" })
public class AccountEntity extends BaseEntity {

    @NotNull(message = "User must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @NotBlank(message = "Password must not be empty")
    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @NotNull(message = "Active status must not be null")
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default_password", nullable = false)
    @Builder.Default
    private Boolean isDefaultPassword = true;
}
