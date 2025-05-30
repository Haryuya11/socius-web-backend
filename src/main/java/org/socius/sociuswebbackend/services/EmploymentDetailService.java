package org.socius.sociuswebbackend.services;

import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface EmploymentDetailService {
    Map<String, Object> getAllEmployees(Pageable pageable);
    Map<String, Object> getAllEmployeesForAdmin(Pageable pageable);
}
