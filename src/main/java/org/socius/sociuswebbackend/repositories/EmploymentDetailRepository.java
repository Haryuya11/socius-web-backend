package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface EmploymentDetailRepository extends JpaRepository<EmploymentDetailEntity, UUID> {


    /**
     * Tìm kiếm thông tin chi của nhân viên theo ID người dùng
     *
     * @param userId ID của người dùng
     * @return Optional chứa thông tin chi tiết nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<EmploymentDetailEntity> findByUserId(UUID userId);

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
    @Query("SELECT COUNT(e) FROM EmploymentDetailEntity e WHERE e.department.id = :departmentId AND e.workingStatus = 'active'")
    long countByDepartmentId(UUID departmentId);

    /**
     * Đếm số nhân viên thuộc team
     *
     * @param teamId ID của team
     * @return Số lượng nhân viên
     */
    @Query("SELECT COUNT(e) FROM EmploymentDetailEntity e WHERE e.team.id = :teamId AND e.workingStatus = 'active'")
    long countByTeamId(UUID teamId);

    /**
     * Đếm số nhân viên có vị trí
     *
     * @param positionId ID của vị trí
     * @return Số lượng nhân viên
     */
    @Query("SELECT COUNT(e) FROM EmploymentDetailEntity e WHERE e.position.id = :positionId AND e.workingStatus = 'active'")
    long countByPositionId(UUID positionId);


    /**
     * Lấy tất cả thông tin chi tiết của nhân viên với phân trang
     *
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách EmploymentDetailEntity
     */
    @Query("SELECT e FROM EmploymentDetailEntity e WHERE e.workingStatus = 'active'")
    Page<EmploymentDetailEntity> findAll(@NonNull Pageable pageable);

    List<EmploymentDetailEntity> findByTeam_Id(UUID teamId);
}
