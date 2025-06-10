package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;


public interface AdminService {
    
    /**
     * Tạo tài khoản nhân viên mới với mật khẩu mặc định
     * 
     * @param requestDto Thông tin nhân viên cần tạo
     * @return Thông tin nhân viên đã tạo
     */
    EmploymentDetailResponseDto createEmployee(EmployeeCreationRequestDto requestDto);
}