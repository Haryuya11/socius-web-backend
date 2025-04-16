package org.socius.sociuswebbackend.repositories;

import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    /**
     * Kiểm tra xem vai trò có tồn tại hay không
     * 
     * @param name Tên vai trò cần kiểm tra
     * @return true nếu vai trò tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);
    
}
