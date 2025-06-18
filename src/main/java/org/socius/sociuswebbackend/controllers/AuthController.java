package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.config.PermissionConstants;
import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.PasswordChangeRequestDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.security.RequirePermission;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.socius.sociuswebbackend.services.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    final private AuthenticationService authenticationService;
    final private JwtTokenService jwtTokenService;
    final private UserRepository userRepository;

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
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponseDto result = authenticationService.login(loginRequest, request, response);
        if (result.isAuthenticated()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
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
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * @param requestDto Thông tin yêu cầu đổi mật khẩu bao gồm mật khẩu hiện tại và
     *                   mật khẩu mới
     * @param request    Request HTTP hiện tại
     * @return HTTP 200 OK nếu đổi mật khẩu thành công, hoặc 400 Bad Request nếu
     * mật khẩu hiện tại không đúng hoặc mật khẩu mới không khớp
     */
    @PostMapping("/change-password")
    @RequirePermission(PermissionConstants.CHANGE_PASSWORD)
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto requestDto,
            HttpServletRequest request) {
        authenticationService.changePassword(requestDto, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Reset mật khẩu cho người dùng thông qua email
     *
     * @param email Email của người dùng cần reset mật khẩu
     * @return HTTP 200 OK nếu gửi email thành công, hoặc 400 Bad Request nếu
     */
    @PostMapping("/reset-password")
    @RequirePermission(PermissionConstants.RESET_USER_PASSWORD)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody String email) {
        authenticationService.resetPassword(email);
        return ResponseEntity.ok().build();
    }

    /**
     * Tạo token cho chatbot để truy cập các API cần thiết
     *
     * @return Token và thông tin liên quan nếu thành công, hoặc 401 Unauthorized nếu không xác thực
     */
    @PostMapping("/chatbot-token")
    public ResponseEntity<Map<String, String>> generateChatbotToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenService.generateChatbotToken(user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("expiresIn", "3600"); // seconds

        return ResponseEntity.ok(response);
    }
}
