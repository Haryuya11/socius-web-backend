package org.socius.sociuswebbackend.repositories;

import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<PositionEntity, UUID> {
    /**
     * Kiểm tra xem vị trí có tồn tại hay không
     * 
     * @param name Tên vị trí cần kiểm tra
     * @return true nếu vị trí tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);
}