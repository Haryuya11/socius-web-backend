package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<PositionEntity, UUID> {
    /**
     * Kiểm tra xem vị trí có tồn tại hay không
     *
     * @param name Tên vị trí cần kiểm tra
     * @return true nếu vị trí tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);

    /**
     * Tìm vị trí cùng với danh sách thành viên của nó
     *
     * @param positionId ID của vị trí cần tìm
     * @param pageable   Thông tin phân trang
     * @return Danh sách thành viên của vị trí nếu tìm thấy, null nếu không tìm thấy
     */
    @Query("SELECT p FROM PositionEntity p JOIN FETCH p.employmentDetails WHERE p.id = :positionId")
    Page<PositionEntity> findPositionWithMembers(UUID positionId, Pageable pageable);

    /**
     * Lấy tất cả các vị trí đang hoạt động
     *
     * @return Danh sách các vị trí đang hoạt động
     */
    @Query("SELECT p FROM PositionEntity p WHERE p.status = 'active'")
    List<PositionEntity> findAllActivePositions();
}