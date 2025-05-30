package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

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

    @GetMapping("/user/{userId}/employment-history")
    public ResponseEntity<Map<String, Object>> getEmploymentHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = employmentDetailService.getEmploymentHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/salary-history")
    public ResponseEntity<Map<String, Object>> getSalaryHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = employmentDetailService.getSalaryHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
