package org.socius.sociuswebbackend.model.dtos.account;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponseDto extends BaseDto {
    private UserResponseDto user;
    private LocalDateTime lastLogin;
    private Boolean isActive;
    private Boolean isDefaultPassword;
}
