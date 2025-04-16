package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;

import java.util.List;
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
}