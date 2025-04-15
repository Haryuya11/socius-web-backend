package org.socius.sociuswebbackend.controllers;

import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;
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
     * @param request Request HTTP hiện tại
     * @param response Response HTTP hiện tại
     * @return Thông tin phản hồi đăng nhập bao gồm thông tin người dùng và trạng thái xác thực
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
     * @param request Request HTTP hiện tại
     * @param response Response HTTP hiện tại
     * @return HTTP 200 OK sau khi đăng xuất thành công
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * Kiểm tra thông tin phiên hiện tại
     * 
     * @param request Request HTTP hiện tại
     * @return Thông tin phiên nếu người dùng đã xác thực, hoặc 401 Unauthorized nếu chưa
     */
    @GetMapping("/session")
    public ResponseEntity<SessionInfoDto> checkSession(HttpServletRequest request) {
        SessionInfoDto sessionInfo = authenticationService.getCurrentSession(request);
        if (sessionInfo != null) {
            return ResponseEntity.ok(sessionInfo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
