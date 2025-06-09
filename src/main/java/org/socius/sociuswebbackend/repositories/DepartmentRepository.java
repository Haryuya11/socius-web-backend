package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, UUID> {
    /**
     * Kiểm tra xem phòng ban có tồn tại hay không
     *
     * @param name Tên phòng ban cần kiểm tra
     * @return true nếu phòng ban tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);

    /**
     * Tìm phòng ban cùng với các thành viên của nó
     *
     * @param departmentId ID của phòng ban cần tìm
     * @param pageable     Thông tin phân trang
     * @return Trang chứa thông tin phòng ban và các thành viên
     */
    @Query("SELECT d FROM DepartmentEntity d JOIN FETCH d.employmentDetail e WHERE d.id = :departmentId")
    Page<DepartmentEntity> findDepartmentWithMembers(UUID departmentId, Pageable pageable);

    /**
     * Lấy tất cả các phòng ban đang hoạt động
     *
     * @return Danh sách các phòng ban đang hoạt động
     */
    @Query("SELECT d FROM DepartmentEntity d WHERE d.status = 'active'")
    List<DepartmentEntity> findAllActiveDepartments();

}
