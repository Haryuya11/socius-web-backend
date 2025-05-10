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
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
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

    /**
     * Lấy danh sách tất cả các vị trí
     *
     * @return Danh sách các vị trí
     */
    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        List<PositionResponseDto> positions = positionService.findAll();
        return ResponseEntity.ok(positions);
    }

    /**
     * Lấy thông tin một vị trí theo ID
     *
     * @param id ID của vị trí cần tìm
     * @return Thông tin vị trí nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionResponseDto> getPositionById(@PathVariable UUID id) {
        PositionResponseDto position = positionService.findById(id);
        return ResponseEntity.ok(position);
    }

    /**
     * Tạo một vị trí mới
     *
     * @param requestDto Thông tin yêu cầu tạo vị trí
     * @return Thông tin vị trí đã được tạo
     */
    @PostMapping("/positions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto createdPosition = positionService.create(requestDto);
        return ResponseEntity.ok(createdPosition);
    }
    
    // DEPARTMENT ENDPOINTS

    /**
     * Lấy danh sách tất cả các phòng ban
     *
     * @return Danh sách các phòng ban
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.findAll();
        return ResponseEntity.ok(departments);
    }

    /**
     * Lấy thông tin một phòng ban theo ID
     *
     * @param id ID của phòng ban cần tìm
     * @return Thông tin phòng ban nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable UUID id) {
        DepartmentResponseDto department = departmentService.findById(id);
        return ResponseEntity.ok(department);
    }

    /**
     * Tạo một phòng ban mới
     *
     * @param requestDto Thông tin yêu cầu tạo phòng ban
     * @return Thông tin phòng ban đã được tạo
     */
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto createdDepartment = departmentService.create(requestDto);
        return ResponseEntity.ok(createdDepartment);
    }
    
    // TEAM ENDPOINTS

    /**
     * Lấy danh sách tất cả các team
     *
     * @return Danh sách các team
     */
    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }

    /**
     * Lấy thông tin một team theo ID
     *
     * @param id ID của team cần tìm
     * @return Thông tin team nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/teams/{id}")
    public ResponseEntity<TeamResponseDto> getTeamById(@PathVariable UUID id) {
        TeamResponseDto team = teamService.findById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * Tạo một team mới
     *
     * @param requestDto Thông tin yêu cầu tạo team
     * @return Thông tin team đã được tạo
     */
    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamRequestDto requestDto) {
        TeamResponseDto createdTeam = teamService.create(requestDto);
        return ResponseEntity.ok(createdTeam);
    }
    
    // ROLE ENDPOINTS

    /**
     * Lấy danh sách tất cả các vai trò
     *
     * @return Danh sách các vai trò
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy thông tin một vai trò theo ID
     *
     * @param id ID của vai trò cần tìm
     * @return Thông tin vai trò nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
        RoleResponseDto role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo một vai trò mới
     *
     * @param requestDto Thông tin yêu cầu tạo vai trò
     * @return Thông tin vai trò đã được tạo
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto createdRole = roleService.create(requestDto);
        return ResponseEntity.ok(createdRole);
    }
}