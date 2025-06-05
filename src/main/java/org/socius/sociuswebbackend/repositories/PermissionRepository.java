package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    /**
     * Tìm permission theo tên
     */
    Optional<PermissionEntity> findByName(String name);

    /**
     * Kiểm tra permission có tồn tại theo tên
     */
    boolean existsByName(String name);

    /**
     * Lấy tất cả permission được sắp xếp theo tên
     */
    @Query("SELECT p FROM PermissionEntity p ORDER BY p.name")
    List<PermissionEntity> findAllOrderByName();
}