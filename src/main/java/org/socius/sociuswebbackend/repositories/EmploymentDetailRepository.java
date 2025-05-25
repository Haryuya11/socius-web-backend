package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentDetailRepository extends JpaRepository<EmploymentDetailEntity, UUID> {
    
    /**
     * Tìm kiếm thông tin chi của nhân viên
     * @param user Đối tượng người dùng
     * @return Optional chứa thông tin chi tiết nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<EmploymentDetailEntity> findByUser(UserEntity user);
}
