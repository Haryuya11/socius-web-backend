package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.employee.EmployeeUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryUpdateRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EmploymentDetailService {

    /**
     * Lấy danh sách tất cả nhân viên
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách nhân viên cùng với thông tin phân trang
     */
    Map<String, Object> getAllEmployees(Pageable pageable);

    /**
     * Lấy danh sách tất cả nhân viên cho trang quản trị
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách nhân viên cùng với thông tin phân trang
     */
    Map<String, Object> getAllEmployeesForAdmin(Pageable pageable);

    /**
     * Gán role cho nhân viên
     *
     * @param employeeId ID của nhân viên
     * @param roleId     ID của role cần gán
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    EmploymentDetailResponseDto assignRoleToEmployee(UUID employeeId, UUID roleId);

    /**
     * Gán role cho nhiều nhân viên
     *
     * @param employeeIds Danh sách ID nhân viên
     * @param roleId      ID của role cần gán
     * @return Danh sách thông tin nhân viên đã được cập nhật
     */
    List<EmploymentDetailResponseDto> assignRoleToMultipleEmployees(List<UUID> employeeIds, UUID roleId);

    /**
     * Xóa role khỏi nhân viên (set về null)
     *
     * @param employeeId ID của nhân viên
     */
    void removeRoleFromEmployee(UUID employeeId);

    /**
     * Xóa role khỏi nhiều nhân viên
     *
     * @param employeeIds Danh sách ID nhân viên
     */
    void removeRoleFromMultipleEmployees(List<UUID> employeeIds);

    /**
     * Lấy danh sách nhân viên theo role
     *
     * @param roleId ID của role
     * @return Danh sách nhân viên có role đó
     */
    List<EmploymentDetailResponseDto> getEmployeesByRole(UUID roleId);

    /**
     * Thêm nhân viên vào team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần thêm
     */
    void addEmployeeToTeam(UUID teamId, UUID employeeId);

    /**
     * Thêm nhiều nhân viên vào team
     *
     * @param teamId      ID của team
     * @param employeeIds Danh sách ID của các nhân viên cần thêm
     */
    void addEmployeesToTeam(UUID teamId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi team
     *
     * @param employeeId ID của nhân viên cần xóa
     */
    void removeEmployeeFromTeam(UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi team
     *
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     */
    void removeEmployeesFromTeam(List<UUID> employeeIds);

    /**
     * Thêm nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần thêm
     */
    void addEmployeeToDepartment(UUID departmentId, UUID employeeId);


    /**
     * Thêm nhiều nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeIds  Danh sách ID của các nhân viên cần thêm
     */
    void addEmployeesToDepartment(UUID departmentId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi phòng ban
     *
     * @param employeeId ID của nhân viên cần xóa
     */
    void removeEmployeeFromDepartment(UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi phòng ban
     *
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     */
    void removeEmployeesFromDepartment(List<UUID> employeeIds);

    /**
     * Thêm nhân viên vào vị trí công việc
     *
     * @param positionId ID của vị trí công việc
     * @param employeeId ID của nhân viên cần thêm
     */
    void addEmployeeToPosition(UUID positionId, UUID employeeId);

    /**
     * Thêm nhiều nhân viên vào vị trí công việc
     *
     * @param positionId  ID của vị trí công việc
     * @param employeeIds Danh sách ID của các nhân viên cần thêm
     */
    void addEmployeesToPosition(UUID positionId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi vị trí công việc
     *
     * @param employeeId ID của nhân viên cần xóa
     */
    void removeEmployeeFromPosition(UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi vị trí công việc
     *
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     */
    void removeEmployeesFromPosition(List<UUID> employeeIds);

    /**
     * Cập nhật lương của nhân viên
     *
     * @param requestDto Thông tin yêu cầu cập nhật lương
     */
    EmploymentDetailResponseDto updateEmployeeSalary(SalaryUpdateRequestDto requestDto, UUID employeeId);

    /**
     * Cập nhật thông tin nhân viên
     *
     * @param employeeId ID của nhân viên cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    EmploymentDetailResponseDto updateEmployee(UUID employeeId, EmployeeUpdateRequestDto requestDto);

    /**
     * Xóa nhân viên (soft delete) - chuyển trạng thái thành terminated
     * và xóa khỏi các assignment hiện tại
     *
     * @param employeeId ID của nhân viên cần xóa
     */
    void terminateEmployee(UUID employeeId);

    /**
     * Xóa nhiều nhân viên cùng lúc
     *
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     */
    void terminateEmployees(List<UUID> employeeIds);

    /**
     * Khôi phục nhân viên đã bị terminate
     *
     * @param employeeId ID của nhân viên cần khôi phục
     */
    void restoreEmployee(UUID employeeId);

    /**
     * Lấy danh sách nhân viên đã bị terminate
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách nhân viên đã bị terminate cùng với thông tin phân trang
     */
    Map<String, Object> getTerminatedEmployees(Pageable pageable);
}
