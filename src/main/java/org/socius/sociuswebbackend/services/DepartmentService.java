package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DepartmentService {

    /**
     * Lấy danh sách tất cả các phòng ban
     *
     * @return Danh sách các phòng ban
     */
    List<DepartmentResponseDto> findAll();

    /**
     * Tìm một phòng ban theo ID
     *
     * @param id ID của phòng ban cần tìm
     * @return Phòng ban nếu tìm thấy, null nếu không tìm thấy
     */
    DepartmentResponseDto findById(UUID id);

    /**
     * Tạo một phòng ban mới
     *
     * @param requestDto Thông tin yêu cầu tạo phòng ban
     * @return Thông tin phòng ban đã được tạo
     */
    DepartmentResponseDto create(DepartmentRequestDto requestDto);

    /**
     * Cập nhật thông tin một phòng ban
     *
     * @param id         ID của phòng ban cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật phòng ban
     * @return Thông tin phòng ban đã được cập nhật
     */
    DepartmentResponseDto update(UUID id, DepartmentRequestDto requestDto);

    /**
     * Xóa một phòng ban
     *
     * @param id ID của phòng ban cần xóa
     */
    void delete(UUID id);


    /**
     * Thêm nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần thêm
     */
    void addEmployee(UUID departmentId, UUID employeeId);


    /**
     * Thêm nhiều nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeIds  Danh sách ID của các nhân viên cần thêm
     */
    List<DepartmentResponseDto> addEmployees(UUID departmentId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần xóa
     */
    void removeEmployee(UUID departmentId, UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeIds  Danh sách ID của các nhân viên cần xóa
     * @return Danh sách thông tin phòng ban sau khi xóa nhân viên
     */
    List<DepartmentResponseDto> removeEmployees(UUID departmentId, List<UUID> employeeIds);

    /**
     * Lấy thông tin phòng ban cùng với danh sách thành viên
     *
     * @param departmentId ID của phòng ban
     * @param pageable     Thông tin phân trang
     * @return Map chứa thông tin phòng ban và danh sách thành viên
     */
    Map<String, Object> getDepartmentWithMembers(UUID departmentId, Pageable pageable);
}