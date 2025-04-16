package org.socius.sociuswebbackend.model.dtos.user;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserDto {
    private UUID userId;
    private String fullName;
    private String imageUrl;
}