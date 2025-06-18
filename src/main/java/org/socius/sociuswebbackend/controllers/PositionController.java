package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.config.PermissionConstants;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.security.RequirePermission;
import org.socius.sociuswebbackend.services.PositionService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {

    final PositionService positionService;

    /**
     * Lấy danh sách tất cả các vị trí
     *
     * @return Danh sách các vị trí
     */
    @GetMapping()
    @RequirePermission(PermissionConstants.POSITION_GET_ALL)
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        List<PositionResponseDto> positions = positionService.findAllActivePositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * Lấy thông tin một vị trí cùng với danh sách thành viên của nó
     *
     * @param positionId ID của vị trí cần tìm
     * @param pageable   Thông tin phân trang
     * @return Thông tin phòng ban cùng với danh sách thành viên nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{positionId}/members")
    @RequirePermission(PermissionConstants.POSITION_GET_ALL)
    public ResponseEntity<Map<String, Object>> getPositionWithMembers(
            @PathVariable UUID positionId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(positionService.getPositionWithMembers(positionId, pageable));
    }

    /**
     * Lấy thông tin một vị trí theo ID
     *
     * @param id ID của vị trí cần tìm
     * @return Thông tin vị trí nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.POSITION_READ)
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
    @PostMapping("/create")
    @RequirePermission(PermissionConstants.POSITION_CREATE)
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto createdPosition = positionService.create(requestDto);
        return ResponseEntity.ok(createdPosition);
    }

    /**
     * Xóa một vị trí theo ID
     *
     * @param positionId ID của vị trí cần xóa
     * @return ResponseEntity với mã trạng thái 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/delete/{positionId}")
    @RequirePermission(PermissionConstants.POSITION_DELETE)
    public ResponseEntity<PositionResponseDto> deletePosition(@PathVariable UUID positionId) {
        positionService.delete(positionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật thông tin một vị trí
     *
     * @param positionId ID của vị trí cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vị trí
     * @return Thông tin vị trí đã được cập nhật
     */
    @PutMapping("/update/{positionId}")
    @RequirePermission(PermissionConstants.POSITION_UPDATE)
    public ResponseEntity<PositionResponseDto> updatePosition(@PathVariable UUID positionId, @Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto updatedPosition = positionService.update(positionId, requestDto);
        return ResponseEntity.ok(updatedPosition);
    }
}
