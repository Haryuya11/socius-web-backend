package org.socius.sociuswebbackend.model.dtos.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequestDto {
    private String name;

    private UUID leaderId;
}
