package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmploymentDetailController {

    private final EmploymentDetailService employmentDetailService;

    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllEmployees(pageable));
    }

    @GetMapping("admin/employees")
    public ResponseEntity<Map<String, Object>> getAllEmployeesForAdmin(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllEmployeesForAdmin(pageable));
    }
}
