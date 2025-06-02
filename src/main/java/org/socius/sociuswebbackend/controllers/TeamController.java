package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    /**
     * Lấy danh sách tất cả các team
     *
     * @return Danh sách các team
     */
    @GetMapping("/")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }


    /**
     * Lấy thông tin một team cùng với danh sách thành viên của nó
     *
     * @param teamId ID của team cần tìm
     * @param pageable Thông tin phân trang
     * @return Thông tin team cùng với danh sách thành viên nếu tìm thấy, null nếu không tìm thấy
     */
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

    /**
     * Lấy thông tin một team theo ID
     *
     * @param teamId ID của team cần tìm
     * @return Thông tin team nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeamById(@PathVariable UUID teamId) {
        TeamResponseDto team = teamService.findById(teamId);
        return ResponseEntity.ok(team);
    }

    /**
     * Tạo một team mới
     *
     * @param requestDto Thông tin yêu cầu tạo team
     * @return Thông tin team đã được tạo
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamRequestDto requestDto) {
        TeamResponseDto createdTeam = teamService.create(requestDto);
        return ResponseEntity.ok(createdTeam);
    }

    /**
     * Xóa một team theo ID
     *
     * @param teamId ID của team cần xóa
     * @return ResponseEntity với mã trạng thái 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/delete/{teamId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<TeamResponseDto> deleteTeam(@PathVariable UUID teamId) {
        teamService.delete(teamId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật thông tin một team
     *
     * @param teamId     ID của team cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật team
     * @return Thông tin team đã được cập nhật
     */
    @PutMapping("/update/{teamId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<TeamResponseDto> updateTeam(@PathVariable UUID teamId, @Valid @RequestBody TeamRequestDto requestDto) {
        TeamResponseDto updatedTeam = teamService.update(teamId, requestDto);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * Thêm một nhân viên vào team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần thêm vào team
     * @return Thông tin team đã được cập nhật
     */
    @PostMapping("/add/{teamId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<TeamResponseDto> addEmployeeToTeam(@PathVariable UUID teamId, @PathVariable UUID employeeId) {
        teamService.addEmployee(teamId, employeeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa một nhân viên khỏi team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần xóa khỏi team
     * @return Thông tin team đã được cập nhật
     */
    @DeleteMapping("/remove/{teamId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> removeEmployeeFromTeam(@PathVariable UUID teamId, @PathVariable UUID employeeId) {
        teamService.removeEmployee(teamId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
