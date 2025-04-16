package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    /**
     * Lấy danh sách tất cả các vai trò
     * 
     * @return Danh sách các vai trò
     */
    List<RoleResponseDto> findAll();

    /**
     * Tìm một vai trò theo ID
     * 
     * @param id ID của vai trò cần tìm
     * @return vai trò nếu tìm thấy, null nếu không tìm thấy
     */
    RoleResponseDto findById(UUID id);

    /**
     * Tạo một vai trò mới
     * 
     * @param requestDto Thông tin yêu cầu tạo vai trò
     * @return Thông tin vai trò đã được tạo
     */
    RoleResponseDto create(RoleRequestDto requestDto);

    /**
     * Cập nhật thông tin một vai trò
     * 
     * @param id         ID của vai trò cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vai trò
     * @return Thông tin vai trò đã được cập nhật
     */
    RoleResponseDto update(UUID id, RoleRequestDto requestDto);

    /**
     * Xóa một vai trò
     * 
     * @param id ID của vai trò cần xóa
     */
    void delete(UUID id);
}