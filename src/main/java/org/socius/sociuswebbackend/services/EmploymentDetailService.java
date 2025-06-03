package org.socius.sociuswebbackend.services;

import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface EmploymentDetailService {
    /**
     * Lấy danh sách tất cả nhân viên với phân trang
     *
     * @param pageable Đối tượng chứa thông tin phân trang (page, size, sort)
     * @return Map chứa danh sách nhân viên và metadata phân trang
     */
    Map<String, Object> getAllEmployees(Pageable pageable);

    /**
     * Lấy danh sách tất cả nhân viên dành cho admin với phân trang
     *
     * @param pageable Đối tượng chứa thông tin phân trang (page, size, sort)
     * @return Map chứa danh sách nhân viên và metadata phân trang dành cho admin
     */
    Map<String, Object> getAllEmployeesForAdmin(Pageable pageable);
}
