package org.socius.sociuswebbackend.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.mappers.EmploymentHistoryMapper;
import org.socius.sociuswebbackend.mappers.SalaryHistoryMapper;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentHistoryResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.socius.sociuswebbackend.model.entities.SalaryHistoryEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.SalaryHistoryRepository;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmploymentDetailServiceImpl implements EmploymentDetailService {
    private final EmploymentDetailRepository employmentDetailRepository;
    private final EmploymentDetailMapper employmentDetailMapper;
    private final EmploymentHistoryRepository employmentHistoryRepository;
    private final EmploymentHistoryMapper employmentHistoryMapper;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final SalaryHistoryMapper salaryHistoryMapper;

    @Override
    public Map<String, Object> getAllActiveEmployees(Pageable pageable) {
        Page<EmploymentDetailEntity> employeePage = employmentDetailRepository.findByWorkingStatus(WorkingStatus.active, pageable);
        List<EmploymentDetailEntity> filteredEmployees = employeePage.getContent().stream()
                .filter(employee -> employee.getTeam() == null)
                .collect(Collectors.toList());

        Page<EmploymentDetailEntity> filteredPage = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());

        List<EmploymentDetailResponseDto> employees = filteredEmployees.stream()
                .map(employmentDetailMapper::entityToLimitedDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("employeeCount", employees.size());
        result.put("totalPages", filteredPage.getTotalPages());
        result.put("totalElements", filteredPage.getTotalElements());

        return result;
    }

    @Override
    public Map<String, Object> getAllActiveEmployeesForAdmin(Pageable pageable) {
        Page<EmploymentDetailEntity> employeePage = employmentDetailRepository.findByWorkingStatus(WorkingStatus.active, pageable);
        List<EmploymentDetailEntity> filteredEmployees = employeePage.getContent().stream()
                .filter(employee -> employee.getTeam() == null)
                .collect(Collectors.toList());

        Page<EmploymentDetailEntity> filteredPage = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());

        List<EmploymentDetailResponseDto> employees = filteredEmployees.stream()
                .map(employmentDetailMapper::entityToLimitedDtoForAdmin)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("employeeCount", employees.size());
        result.put("totalPages", filteredPage.getTotalPages());
        result.put("totalElements", filteredPage.getTotalElements());

        return result;
    }
}
