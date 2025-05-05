package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
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

    /**
     * Tìm vai trò theo tên
     *
     * @param adminRoleName
     * @return Optional<RoleEntity> nếu tìm thấy vai trò, Optional.empty() nếu không tìm thấy
     */
    Optional<RoleEntity> findByName(String adminRoleName);
}
