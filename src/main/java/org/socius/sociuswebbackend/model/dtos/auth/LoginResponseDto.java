package org.socius.sociuswebbackend.model.dtos.auth;

import java.util.Set;

import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private UserResponseDto user;
    private Set<PermissionResponseDto> permissions;
    private String sessionId;
    private boolean authenticated;
    private String message;
    private boolean passwordChangeRequired;
}
