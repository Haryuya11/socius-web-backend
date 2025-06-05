package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Tìm người dùng theo địa chỉ email
     *
     * @param email Địa chỉ email của người dùng cần tìm
     * @return Optional chứa người dùng nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Tìm danh sách người dùng không thuộc bất kỳ team nào
     *
     * @return Danh sách người dùng không thuộc bất kỳ team nào
     */
    @Query("SELECT u FROM UserEntity u join EmploymentDetailEntity e ON u.id = e.user.id " +
            "WHERE e.team IS NULL + e.workingStatus = 'active'")
    List<UserEntity> findUsersNotInAnyTeam();

    /**
     * Tìm danh sách người dùng không thuộc bất kỳ phòng ban nào
     *
     * @return Danh sách người dùng không thuộc bất kỳ phòng ban nào
     */
    @Query("SELECT u FROM UserEntity u join EmploymentDetailEntity e ON u.id = e.user.id " +
            "WHERE e.department IS NULL + e.workingStatus = 'active'")
    List<UserEntity> findUsersNotInAnyDepartment();

    /**
     * Tìm danh sách người dùng không thuộc bất kỳ vị trí nào
     *
     * @return Danh sách người dùng không thuộc bất kỳ vị trí nào
     */
    @Query("SELECT u FROM UserEntity u join EmploymentDetailEntity e ON u.id = e.user.id " +
            "WHERE e.position IS NULL + e.workingStatus = 'active'")
    List<UserEntity> findUsersNotInAnyPosition();

    @Query("SELECT u FROM UserEntity u " +
            "INNER JOIN EmploymentDetailEntity ed ON u.id = ed.user.id " +
            "WHERE ed.workingStatus = 'active'")
    List<UserEntity> findAllActiveUsers();
}
