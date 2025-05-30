package org.socius.sociuswebbackend.services;

import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface EmploymentDetailService {
    Map<String, Object> getAllEmployees(Pageable pageable);
    Map<String, Object> getAllEmployeesForAdmin(Pageable pageable);
    Map<String, Object> getEmploymentHistory(UUID userId, Pageable pageable);
    Map<String, Object> getSalaryHistory(UUID userId, Pageable pageable);
}
