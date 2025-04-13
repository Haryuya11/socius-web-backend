package org.socius.sociuswebbackend.model.dtos.auth;

import lombok.Data;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;

import java.util.Set;

@Data
public class LoginResponseDto {
    private UserResponseDto user;
    private Set<PermissionResponseDto> permissions;
    private String sessionId;
    private boolean authenticated;
    private String message;
}
