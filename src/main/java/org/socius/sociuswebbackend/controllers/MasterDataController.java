package org.socius.sociuswebbackend.controllers;

import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.socius.sociuswebbackend.services.PositionService;
import org.socius.sociuswebbackend.services.RoleService;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private PositionService positionService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private TeamService teamService;
    
    // POSITION ENDPOINTS
    
    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        List<PositionResponseDto> positions = positionService.findAll();
        return ResponseEntity.ok(positions);
    }
    
    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionResponseDto> getPositionById(@PathVariable UUID id) {
        PositionResponseDto position = positionService.findById(id);
        return ResponseEntity.ok(position);
    }
    
    @PostMapping("/positions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto createdPosition = positionService.create(requestDto);
        return ResponseEntity.ok(createdPosition);
    }
    
    // DEPARTMENT ENDPOINTS
    
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.findAll();
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable UUID id) {
        DepartmentResponseDto department = departmentService.findById(id);
        return ResponseEntity.ok(department);
    }
    
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto createdDepartment = departmentService.create(requestDto);
        return ResponseEntity.ok(createdDepartment);
    }
    
    // TEAM ENDPOINTS
    
    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }
    
    @GetMapping("/teams/{id}")
    public ResponseEntity<TeamResponseDto> getTeamById(@PathVariable UUID id) {
        TeamResponseDto team = teamService.findById(id);
        return ResponseEntity.ok(team);
    }
    
    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamRequestDto requestDto) {
        TeamResponseDto createdTeam = teamService.create(requestDto);
        return ResponseEntity.ok(createdTeam);
    }
    
    // ROLE ENDPOINTS
    
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
        RoleResponseDto role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }
    
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto createdRole = roleService.create(requestDto);
        return ResponseEntity.ok(createdRole);
    }
}