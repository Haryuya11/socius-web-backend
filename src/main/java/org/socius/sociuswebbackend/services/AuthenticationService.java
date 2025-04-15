package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    /**
     * Xác thực người dùng và tạo phiên đăng nhập
     * 
     * @param loginRequestDto Thông tin đăng nhập từ người dùng
     * @param request Request HTTP hiện tại
     * @param response Response HTTP hiện tại
     * @return Thông tin phản hồi đăng nhập chứa trạng thái xác thực và thông tin người dùng
     */
    LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response);

    /**
     * Đăng xuất người dùng và hủy phiên hiện tại
     * 
     * @param request Request HTTP hiện tại
     * @param response Response HTTP hiện tại
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * Lấy thông tin phiên hiện tại của người dùng
     * 
     * @param request Request HTTP hiện tại
     * @return Thông tin phiên làm việc hoặc null nếu không có phiên hợp lệ
     */
    SessionInfoDto getCurrentSession(HttpServletRequest request);

    /**
     * Kiểm tra xem người dùng hiện tại đã được xác thực hay chưa
     * 
     * @param request Request HTTP hiện tại
     * @return true nếu người dùng đã được xác thực, false nếu chưa
     */
    boolean isAuthenticated(HttpServletRequest request);
}
