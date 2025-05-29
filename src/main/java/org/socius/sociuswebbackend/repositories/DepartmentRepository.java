package org.socius.sociuswebbackend.repositories;

import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, UUID> {
    /**
     * Kiểm tra xem phòng ban có tồn tại hay không
     *
     * @param name Tên phòng ban cần kiểm tra
     * @return true nếu phòng ban tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);
}
