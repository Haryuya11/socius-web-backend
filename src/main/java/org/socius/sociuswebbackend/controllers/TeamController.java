package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/{teamId}/members")
    public ResponseEntity<Map<String, Object>> getTeamWithMembers(
            @PathVariable UUID teamId,
            Pageable pageable
    ) {
        if (teamId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(teamService.getTeamWithMembers(teamId, pageable));
    }
}
