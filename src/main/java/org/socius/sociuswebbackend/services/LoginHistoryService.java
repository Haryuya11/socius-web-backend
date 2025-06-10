package org.socius.sociuswebbackend.services;

import java.util.List;
import java.util.UUID;

import org.socius.sociuswebbackend.model.dtos.loginHistory.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.loginHistory.LoginHistoryResponseDto;

public interface LoginHistoryService {
    
    /**
     * Tạo bản ghi lịch sử đăng nhập mới
     * 
     * @param requestDto Thông tin yêu cầu tạo lịch sử đăng nhập
     * @return Thông tin lịch sử đăng nhập đã được tạo
     */
    LoginHistoryResponseDto createLoginHistory(LoginHistoryRequestDto requestDto);

    /**
     * Lấy danh sách lịch sử đăng nhập của một người dùng
     * 
     * @param userId ID của người dùng cần lấy lịch sử
     * @return Danh sách lịch sử đăng nhập của người dùng
     */
    List<LoginHistoryResponseDto> getLoginHistoryByUserId(UUID userId);
}
