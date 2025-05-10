package org.socius.sociuswebbackend.controllers;

import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.PasswordChangeRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;
import org.socius.sociuswebbackend.model.enums.PasswordChangeResult;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Xác thực người dùng và tạo phiên đăng nhập
     *
     * @param loginRequest Thông tin đăng nhập (email và mật khẩu)
     * @param request      Request HTTP hiện tại
     * @param response     Response HTTP hiện tại
     * @return Thông tin phản hồi đăng nhập bao gồm thông tin người dùng và trạng
     * thái xác thực
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponseDto result = authenticationService.login(loginRequest, request, response);
        if (result.isAuthenticated()) {
            return ResponseEntity.ok(result);
        } else if(result.getMessage().contains("Không tìm thấy người dùng")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getMessage());
        } else if (result.getMessage().contains("Sai mật khẩu")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getMessage());
        }
    }

    /**
     * Đăng xuất người dùng và hủy phiên hiện tại
     *
     * @param request  Request HTTP hiện tại
     * @param response Response HTTP hiện tại
     * @return HTTP 200 OK sau khi đăng xuất thành công
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        if (!authenticationService.isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập");
        }

        authenticationService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * Kiểm tra thông tin phiên hiện tại
     *
     * @param request Request HTTP hiện tại
     * @return Thông tin phiên nếu người dùng đã xác thực, hoặc 401 Unauthorized nếu
     * chưa
     */
    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        SessionInfoDto sessionInfo = authenticationService.getCurrentSession(request);
        if (sessionInfo != null) {
            return ResponseEntity.ok(sessionInfo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập");
        }
    }

    /**
     * @param requestDto Thông tin yêu cầu đổi mật khẩu bao gồm mật khẩu hiện tại và
     *                   mật khẩu mới
     * @param request    Request HTTP hiện tại
     * @return HTTP 200 OK nếu đổi mật khẩu thành công, hoặc 400 Bad Request nếu
     * mật khẩu hiện tại không đúng hoặc mật khẩu mới không khớp
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto requestDto,
            HttpServletRequest request) {
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Password vầ Confirm password không khớp");
        }

        PasswordChangeResult result = authenticationService.changePassword(requestDto, request);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok("Đổi mật khẩu thành công");
            case NOT_AUTHENTICATED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập");
            case USER_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người dùng không tồn tại");
            case ACCOUNT_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tài khoản không tồn tại");
            case INCORRECT_PASSWORD ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mật khẩu hiện tại không đúng");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        };
    }
}
