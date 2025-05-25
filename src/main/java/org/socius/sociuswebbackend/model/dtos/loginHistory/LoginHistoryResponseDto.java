package org.socius.sociuswebbackend.model.dtos.loginHistory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LoginHistoryResponseDto extends BaseDto {
    private UserResponseDto user;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String deviceInfo;
}
