package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentDetailRepository extends JpaRepository<EmploymentDetailEntity, UUID> {
    
    /**
     * Tìm kiếm thông tin chi của nhân viên
     * @param user Đối tượng người dùng
     * @return Optional chứa thông tin chi tiết nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<EmploymentDetailEntity> findByUser(UserEntity user);

    /**
     * Lấy tất cả thông tin chi tiết của nhân viên với phân trang
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách EmploymentDetailEntity
     */
    Page<EmploymentDetailEntity> findAll(Pageable pageable);
}
