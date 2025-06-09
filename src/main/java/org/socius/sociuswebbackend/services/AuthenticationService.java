package org.socius.sociuswebbackend.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.socius.sociuswebbackend.model.dtos.auth.*;

public interface AuthenticationService {

    /**
     * Xác thực người dùng và tạo phiên đăng nhập
     *
     * @param loginRequestDto Thông tin đăng nhập từ người dùng
     * @param request         Request HTTP hiện tại
     * @param response        Response HTTP hiện tại
     * @return Thông tin phản hồi đăng nhập chứa trạng thái xác thực và thông tin người dùng
     */
    LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response);

    /**
     * Đăng xuất người dùng và hủy phiên hiện tại
     *
     * @param request  Request HTTP hiện tại
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

    /**
     * Đổi mật khẩu lần đầu sau khi đăng nhập với mật khẩu mặc định
     *
     * @param requestDto Thông tin mật khẩu mới
     * @param request    Request HTTP hiện tại
     */
    void changePassword(PasswordChangeRequestDto requestDto, HttpServletRequest request);

    /**
     * Lấy thông tin quyền hạn của người dùng hiện tại
     *
     * @param request Request HTTP hiện tại
     * @return Thông tin quyền hạn của người dùng hoặc null nếu không có phiên hợp lệ
     */
    UserPermissionsDto getCurrentUserPermissions(HttpServletRequest request);

    /**
     * Kiểm tra xem người dùng có quyền cụ thể hay không
     *
     * @param request    Request HTTP hiện tại
     * @param permission Quyền cần kiểm tra
     * @return true nếu người dùng có quyền, false nếu không
     */
    boolean hasPermission(HttpServletRequest request, String permission);

    /**
     * Gia hạn phiên làm việc của người dùng
     *
     * @param request Request HTTP hiện tại
     * @return true nếu gia hạn thành công, false nếu không
     */
    boolean extendSession(HttpServletRequest request);

    /**
     * Đặt lại mật khẩu cho người dùng thông qua email
     *
     * @param email Email của người dùng cần đặt lại mật khẩu
     */
    void resetPassword(String email);
}
