package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmploymentDetailController {

    private final EmploymentDetailService employmentDetailService;

    /**
     * Lấy danh sách tất cả nhân viên với phân trang
     *
     * @param pageable Đối tượng chứa thông tin phân trang (page, size)
     * @return ResponseEntity chứa Map với danh sách nhân viên và metadata phân trang,
     *         với mã trạng thái HTTP 200 (OK)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllActiveEmployees(pageable));
    }

    /**
     * Lấy danh sách tất cả nhân viên dành cho admin với phân trang
     *
     * @param pageable Đối tượng chứa thông tin phân trang (page, size)
     * @return ResponseEntity chứa Map với danh sách nhân viên và metadata phân trang,
     *         với mã trạng thái HTTP 200 (OK), chỉ dành cho người dùng có quyền admin
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<Map<String, Object>> getAllEmployeesForAdmin(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllActiveEmployeesForAdmin(pageable));
    }
}
