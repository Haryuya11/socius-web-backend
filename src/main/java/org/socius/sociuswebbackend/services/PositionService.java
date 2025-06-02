package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PositionService {

    /**
     * Lấy danh sách tất cả các vị trí
     *
     * @return Danh sách các vị trí
     */
    List<PositionResponseDto> findAll();

    /**
     * Tìm một vị trí theo ID
     *
     * @param id ID của vị trí cần tìm
     * @return Vị trí nếu tìm thấy, null nếu không tìm thấy
     */
    PositionResponseDto findById(UUID id);

    /**
     * Tạo một vị trí mới
     *
     * @param requestDto Thông tin yêu cầu tạo vị trí
     * @return Thông tin vị trí đã được tạo
     */
    PositionResponseDto create(PositionRequestDto requestDto);

    /**
     * Cập nhật thông tin một vị trí
     *
     * @param id         ID của vị trí cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vị trí
     * @return Thông tin vị trí đã được cập nhật
     */
    PositionResponseDto update(UUID id, PositionRequestDto requestDto);

    /**
     * Xóa một vị trí
     *
     * @param id ID của vị trí cần xóa
     */
    void delete(UUID id);


    /**
     * Thêm nhân viên vào vị trí
     *
     * @param positionId ID của vị trí
     * @param employeeId ID của nhân viên cần thêm
     */
    void addEmployee(UUID positionId, UUID employeeId);

    /**
     * Thêm nhiều nhân viên vào vị trí
     *
     * @param positionId  ID của vị trí
     * @param employeeIds Danh sách ID của các nhân viên cần thêm
     * @return Danh sách thông tin vị trí sau khi thêm nhân viên
     */
    List<PositionResponseDto> addEmployees(UUID positionId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi vị trí
     *
     * @param positionId ID của vị trí
     * @param employeeId ID của nhân viên cần xóa
     */
    void removeEmployee(UUID positionId, UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi vị trí
     *
     * @param positionId  ID của vị trí
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     * @return Danh sách thông tin vị trí sau khi xóa nhân viên
     */
    List<PositionResponseDto> removeEmployees(UUID positionId, List<UUID> employeeIds);

    /**
     * Lấy thông tin vị trí cùng với danh sách thành viên
     *
     * @param positionId ID của vị trí
     * @param pageable   Thông tin phân trang
     * @return Map chứa thông tin vị trí và danh sách thành viên
     */
    Map<String, Object> getPositionWithMembers(UUID positionId, Pageable pageable);
}