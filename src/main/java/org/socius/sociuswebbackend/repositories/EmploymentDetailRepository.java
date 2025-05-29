package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentDetailRepository extends JpaRepository<EmploymentDetailEntity, UUID> {

    /**
     * Tìm kiếm thông tin chi của nhân viên
     *
     * @param user Đối tượng người dùng
     * @return Optional chứa thông tin chi tiết nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<EmploymentDetailEntity> findByUser(UserEntity user);

    /**
     * Đếm số nhân viên thuộc phòng ban
     *
     * @param departmentId ID của phòng ban
     * @return Số lượng nhân viên
     */
    long countByDepartmentId(UUID departmentId);

    /**
     * Đếm số nhân viên thuộc team
     *
     * @param teamId ID của team
     * @return Số lượng nhân viên
     */
    long countByTeamId(UUID teamId);

    /**
     * Đếm số nhân viên có vị trí
     *
     * @param positionId ID của vị trí
     * @return Số lượng nhân viên
     */
    long countByPositionId(UUID positionId);

}
