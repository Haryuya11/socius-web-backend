package org.socius.sociuswebbackend.model.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LoginResponseDto {
    private UserResponseDto user;
    private RoleResponseDto role;
    private String sessionId;
    private boolean authenticated;
    private boolean passwordChangeRequired;
}
