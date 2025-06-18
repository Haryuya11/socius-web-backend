package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.config.PermissionConstants;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.security.RequirePermission;
import org.socius.sociuswebbackend.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@RequirePermission(PermissionConstants.CHATBOT_ACCESS)
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    private final TeamService teamService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PositionService positionService;
    private final TaskService taskService;
    private final AdminService adminService;
    private final EmploymentDetailService employmentDetailService;
    private final EmployeeTransferService employeeTransferService;

    // ===================== TEAM MANAGEMENT =====================

    /**
     * Tạo team mới qua chatbot
     */
    @PostMapping("/team/create")
    public ResponseEntity<TeamResponseDto> createTeam(
            @Valid @RequestBody TeamRequestDto requestDto,
            HttpServletRequest request) {

        // Lấy user từ token thay vì SecurityContext
        UserEntity creator = (UserEntity) request.getAttribute("CHATBOT_USER");
        if (creator == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }

        // Set leaderId từ token user
        requestDto.setLeaderId(creator.getId());

        TeamResponseDto createdTeam = teamService.create(requestDto);
        logger.info("Chatbot: Tạo team thành công - {} bởi {}",
                createdTeam.getName(), creator.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    /**
     * Cập nhật thông tin team qua chatbot
     */
    @PutMapping("/team/update/{teamId}")
    public ResponseEntity<TeamResponseDto> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamRequestDto requestDto) {
        TeamResponseDto updatedTeam = teamService.update(teamId, requestDto);
        logger.info("Chatbot: Cập nhật team thành công - {}", updatedTeam.getName());
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * Xóa team qua chatbot
     */
    @DeleteMapping("/team/delete/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        teamService.delete(teamId);
        logger.info("Chatbot: Xóa team thành công - ID: {}", teamId);
        return ResponseEntity.noContent().build();
    }

    // ===================== DEPARTMENT MANAGEMENT =====================

    /**
     * Tạo phòng ban mới qua chatbot
     */
    @PostMapping("/department/create")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto createdDepartment = departmentService.create(requestDto);
        logger.info("Chatbot: Tạo phòng ban thành công - {}", createdDepartment.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    /**
     * Cập nhật thông tin phòng ban qua chatbot
     */
    @PutMapping("/department/update/{departmentId}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto updatedDepartment = departmentService.update(departmentId, requestDto);
        logger.info("Chatbot: Cập nhật phòng ban thành công - {}", updatedDepartment.getName());
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Xóa phòng ban qua chatbot
     */
    @DeleteMapping("/department/delete/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID departmentId) {
        departmentService.delete(departmentId);
        logger.info("Chatbot: Xóa phòng ban thành công - ID: {}", departmentId);
        return ResponseEntity.noContent().build();
    }

    // ===================== ROLE MANAGEMENT =====================

    /**
     * Tạo role mới qua chatbot
     */
    @PostMapping("/role/create")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto createdRole = roleService.create(requestDto);
        logger.info("Chatbot: Tạo role thành công - {}", createdRole.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    /**
     * Cập nhật thông tin role qua chatbot
     */
    @PutMapping("/role/update/{roleId}")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto updatedRole = roleService.update(roleId, requestDto);
        logger.info("Chatbot: Cập nhật role thành công - {}", updatedRole.getName());
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Xóa role qua chatbot
     */
    @DeleteMapping("/role/delete/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        roleService.delete(roleId);
        logger.info("Chatbot: Xóa role thành công - ID: {}", roleId);
        return ResponseEntity.noContent().build();
    }

    // ===================== POSITION MANAGEMENT =====================

    /**
     * Tạo vị trí mới qua chatbot
     */
    @PostMapping("/position/create")
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto createdPosition = positionService.create(requestDto);
        logger.info("Chatbot: Tạo vị trí thành công - {}", createdPosition.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPosition);
    }

    /**
     * Cập nhật thông tin vị trí qua chatbot
     */
    @PutMapping("/position/update/{positionId}")
    public ResponseEntity<PositionResponseDto> updatePosition(
            @PathVariable UUID positionId,
            @Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto updatedPosition = positionService.update(positionId, requestDto);
        logger.info("Chatbot: Cập nhật vị trí thành công - {}", updatedPosition.getName());
        return ResponseEntity.ok(updatedPosition);
    }

    /**
     * Xóa vị trí qua chatbot
     */
    @DeleteMapping("/position/delete/{positionId}")
    public ResponseEntity<Void> deletePosition(@PathVariable UUID positionId) {
        positionService.delete(positionId);
        logger.info("Chatbot: Xóa vị trí thành công - ID: {}", positionId);
        return ResponseEntity.noContent().build();
    }

    // ===================== TASK MANAGEMENT =====================

    /**
     * Tạo task mới qua chatbot
     */
    @PostMapping("/task/create")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto createdTask = taskService.createTask(requestDto);
        logger.info("Chatbot: Tạo task thành công - {}", createdTask.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * Cập nhật trạng thái task qua chatbot
     */
    @PutMapping("/task/update-status/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestParam String status) {
        TaskResponseDto updatedTask = taskService.updateTaskStatus(taskId, status);
        logger.info("Chatbot: Cập nhật trạng thái task thành công - {} -> {}", updatedTask.getName(), status);
        return ResponseEntity.ok(updatedTask);
    }

    // ===================== EMPLOYEE MANAGEMENT =====================

    /**
     * Tạo nhân viên mới qua chatbot
     */
    @PostMapping("/employee/create")
    public ResponseEntity<EmploymentDetailResponseDto> createEmployee(@Valid @RequestBody EmployeeCreationRequestDto requestDto) {
        EmploymentDetailResponseDto createdEmployee = adminService.createEmployee(requestDto);
        logger.info("Chatbot: Tạo nhân viên thành công - {}", createdEmployee.getUser().getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    /**
     * Cập nhật thông tin nhân viên qua chatbot
     */
    @PutMapping("/employee/update/{employeeId}")
    public ResponseEntity<EmploymentDetailResponseDto> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeUpdateRequestDto requestDto) {
        EmploymentDetailResponseDto updatedEmployee = employmentDetailService.updateEmployee(employeeId, requestDto);
        logger.info("Chatbot: Cập nhật nhân viên thành công - {}", updatedEmployee.getUser().getEmail());
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Gán role cho nhân viên qua chatbot
     */
    @PostMapping("/employee/{employeeId}/assign-role/{roleId}")
    public ResponseEntity<EmploymentDetailResponseDto> assignRoleToEmployee(
            @PathVariable UUID employeeId,
            @PathVariable UUID roleId) {
        EmploymentDetailResponseDto updatedEmployee = employmentDetailService.assignRoleToEmployee(employeeId, roleId);
        logger.info("Chatbot: Gán role thành công - {} -> {}",
                updatedEmployee.getUser().getEmail(), updatedEmployee.getRole().getName());
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Xóa role khỏi nhân viên qua chatbot
     */
    @DeleteMapping("/employee/{employeeId}/remove-role")
    public ResponseEntity<Void> removeRoleFromEmployee(@PathVariable UUID employeeId) {
        employmentDetailService.removeRoleFromEmployee(employeeId);
        logger.info("Chatbot: Xóa role khỏi nhân viên thành công - {}", employeeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Chuyển nhân viên sang phòng ban mới qua chatbot
     */
    @PostMapping("/employee/{employeeId}/transfer-department/{newDepartmentId}")
    public ResponseEntity<EmploymentDetailResponseDto> transferEmployeeDepartment(
            @PathVariable UUID employeeId,
            @PathVariable UUID newDepartmentId) {
        EmploymentDetailResponseDto transferredEmployee = employeeTransferService.transferDepartment(employeeId, newDepartmentId);
        logger.info("Chatbot: Chuyển phòng ban thành công - {} -> {}",
                transferredEmployee.getUser().getEmail(), transferredEmployee.getDepartment().getName());
        return ResponseEntity.ok(transferredEmployee);
    }

    /**
     * Chuyển nhân viên sang team mới qua chatbot
     */
    @PostMapping("/employee/{employeeId}/transfer-team/{newTeamId}")
    public ResponseEntity<EmploymentDetailResponseDto> transferEmployeeTeam(
            @PathVariable UUID employeeId,
            @PathVariable UUID newTeamId) {
        EmploymentDetailResponseDto transferredEmployee = employeeTransferService.transferTeam(employeeId, newTeamId);
        logger.info("Chatbot: Chuyển team thành công - {} -> {}",
                transferredEmployee.getUser().getEmail(),
                transferredEmployee.getTeam() != null ? transferredEmployee.getTeam().getName() : "null");
        return ResponseEntity.ok(transferredEmployee);
    }

    /**
     * Chuyển nhân viên sang vị trí mới qua chatbot
     */
    @PostMapping("/employee/{employeeId}/transfer-position/{newPositionId}")
    public ResponseEntity<EmploymentDetailResponseDto> transferEmployeePosition(
            @PathVariable UUID employeeId,
            @PathVariable UUID newPositionId) {
        EmploymentDetailResponseDto transferredEmployee = employeeTransferService.transferPosition(employeeId, newPositionId);
        logger.info("Chatbot: Chuyển vị trí thành công - {} -> {}",
                transferredEmployee.getUser().getEmail(), transferredEmployee.getPosition().getName());
        return ResponseEntity.ok(transferredEmployee);
    }

    /**
     * Terminate nhân viên qua chatbot
     */
    @PostMapping("/employee/{employeeId}/terminate")
    public ResponseEntity<Void> terminateEmployee(@PathVariable UUID employeeId) {
        employmentDetailService.terminateEmployee(employeeId);
        logger.info("Chatbot: Terminate nhân viên thành công - {}", employeeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Khôi phục nhân viên đã terminate qua chatbot
     */
    @PostMapping("/employee/{employeeId}/restore")
    public ResponseEntity<Void> restoreEmployee(@PathVariable UUID employeeId) {
        employmentDetailService.restoreEmployee(employeeId);
        logger.info("Chatbot: Khôi phục nhân viên thành công - {}", employeeId);
        return ResponseEntity.ok().build();
    }

    // ===================== BULK OPERATIONS =====================

    /**
     * Gán role cho nhiều nhân viên qua chatbot
     */
    @PostMapping("/employee/assign-role-bulk/{roleId}")
    public ResponseEntity<List<EmploymentDetailResponseDto>> assignRoleToMultipleEmployees(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> employeeIds) {
        List<EmploymentDetailResponseDto> updatedEmployees = employmentDetailService.assignRoleToMultipleEmployees(employeeIds, roleId);
        logger.info("Chatbot: Gán role hàng loạt thành công - {} nhân viên", updatedEmployees.size());
        return ResponseEntity.ok(updatedEmployees);
    }

    /**
     * Xóa role khỏi nhiều nhân viên qua chatbot
     */
    @DeleteMapping("/employee/remove-role-bulk")
    public ResponseEntity<Void> removeRoleFromMultipleEmployees(@RequestBody List<UUID> employeeIds) {
        employmentDetailService.removeRoleFromMultipleEmployees(employeeIds);
        logger.info("Chatbot: Xóa role hàng loạt thành công - {} nhân viên", employeeIds.size());
        return ResponseEntity.noContent().build();
    }

    /**
     * Terminate nhiều nhân viên qua chatbot
     */
    @PostMapping("/employee/terminate-bulk")
    public ResponseEntity<Void> terminateMultipleEmployees(@RequestBody List<UUID> employeeIds) {
        employmentDetailService.terminateEmployees(employeeIds);
        logger.info("Chatbot: Terminate hàng loạt thành công - {} nhân viên", employeeIds.size());
        return ResponseEntity.noContent().build();
    }

    // ===================== UTILITY ENDPOINTS =====================

    /**
     * Lấy danh sách tất cả teams để chatbot có thể tham khảo
     */
    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.findAllActiveTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * Lấy danh sách tất cả departments để chatbot có thể tham khảo
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.findAllActiveDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * Lấy danh sách tất cả roles để chatbot có thể tham khảo
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.findAllActiveRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy danh sách tất cả positions để chatbot có thể tham khảo
     */
    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        List<PositionResponseDto> positions = positionService.findAllActivePositions();
        return ResponseEntity.ok(positions);
    }
}