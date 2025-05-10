package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
     * @param roleName
     * @return Optional<RoleEntity> nếu tìm thấy vai trò, Optional.empty() nếu không tìm thấy
     */
    Optional<RoleEntity> findByName(String roleName);


    /**
     * Lấy tất cả vai trò kèm theo quyền hạn
     *
     * @return Danh sách các vai trò đã được tải cùng với quyền hạn
     */
    @Query("""
             SELECT r FROM RoleEntity r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission
            """)
    List<RoleEntity> findAllWithPermissions();
}
