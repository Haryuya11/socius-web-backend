package org.socius.sociuswebbackend.repositories;

import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    /**
     * Kiểm tra xem nhóm có tồn tại hay không
     * 
     * @param name Tên nhóm cần kiểm tra
     * @return true nếu nhóm tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);
}
