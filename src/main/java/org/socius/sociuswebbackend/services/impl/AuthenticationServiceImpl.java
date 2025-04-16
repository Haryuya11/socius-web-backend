package org.socius.sociuswebbackend.services.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.UserMapper;
import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.PasswordChangeRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.AccountRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.socius.sociuswebbackend.services.LoginHistoryService;
import org.socius.sociuswebbackend.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final String SESSION_USER_KEY = "USER_ID";
    private static final String SESSION_TEAM_KEY = "TEAM_ID";
    private static final String SESSION_ROLE_KEY = "ROLE_ID";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginHistoryService loginHistoryService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponseDto responseDto = new LoginResponseDto();

        try {
            // 1. Xác thực thông tin đăng nhập
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            // 2. Lưu thông tin người dùng vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lưu security context vào session
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            // 4. Tìm thông tin người dùng
            Optional<UserEntity> userOptional = userRepository
                    .findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                responseDto.setAuthenticated(false);
                responseDto.setMessage("Không tìm thấy người dùng");
                return responseDto;
            }

            UserEntity user = userOptional.get();
            AccountEntity account = user.getAccount();

            // 5. Kiểm tra nếu người dùng đang sử dụng mật khẩu mặc định
            if (account != null && account.getIsDefaultPassword() != null && account.getIsDefaultPassword()) {
                responseDto.setPasswordChangeRequired(true);
            }

            // 6. Cập nhật thời gian đăng nhập cuối cùng
            if (account != null) {
                account.setLastLogin(LocalDateTime.now());
                accountRepository.save(account);
            }

            // 7. Lưu thông tin vào session
            session.setAttribute(SESSION_USER_KEY, user.getId());
            if (user.getEmploymentDetail() != null) {
                EmploymentDetailEntity employmentDetail = user.getEmploymentDetail();

                if (employmentDetail.getTeam() != null) {
                    session.setAttribute(SESSION_TEAM_KEY, employmentDetail.getTeam().getId());
                }

                if (employmentDetail.getRole() != null) {
                    session.setAttribute(SESSION_ROLE_KEY, employmentDetail.getRole().getId());
                }
            }

            // 8. Lấy danh sách các quyền của người dùng
            Set<String> permissions = new HashSet<>();
            if (user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
                permissions = user.getEmploymentDetail().getRole().getRolePermissions().stream()
                        .map(RolePermissionEntity::getPermission)
                        .map(permission -> permission.getName())
                        .collect(Collectors.toSet());
            }

            // 9. Lưu thông tin lịch sử đăng nhập
            String ipAddress = getClientIp(request);
            String deviceInfo = request.getHeader("User-Agent");

            LoginHistoryRequestDto historyDto = LoginHistoryRequestDto.builder()
                    .userId(user.getId())
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .build();

            loginHistoryService.createLoginHistory(historyDto);

            // 10. Gửi thông báo đến WebSocket
            webSocketService.sendUserLoginNotification(user.getFirstName() + " " + user.getLastName());

            // 11. Tạo đối tượng phản hồi
            responseDto.setUser(userMapper.entityToDto(user));
            responseDto.setAuthenticated(true);
            responseDto.setSessionId(session.getId());
            responseDto.setMessage("Đăng nhập thành công");

            return responseDto;

        } catch (AuthenticationException e) {
            logger.error("Lỗi khi đăng nhập: {}", e.getMessage());
            responseDto.setAuthenticated(false);
            responseDto.setMessage("Đăng nhập thất bại: " + e.getMessage());
            return responseDto;
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Lấy thông tin xác thực từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Xóa thông tin xác thực
        if (authentication != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                logger.info("Người dùng {} đã đăng xuất", authentication.getName());

                new SecurityContextLogoutHandler().logout(request, response, authentication);
                session.invalidate();
            }
        }
    }

    @Override
    public SessionInfoDto getCurrentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        UUID userId = (UUID) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            return null;
        }

        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return null;
        }

        UserEntity user = userOptional.get();

        SessionInfoDto sessionInfo = new SessionInfoDto();
        sessionInfo.setUserId(user.getId());
        sessionInfo.setUsername(user.getEmail());
        sessionInfo.setFullName(user.getFirstName() + " " + user.getLastName());
        sessionInfo.setEmail(user.getEmail());
        sessionInfo.setImageUrl(user.getImageUrl());
        sessionInfo.setSessionId(session.getId());
        sessionInfo.setSessionCreationTime(LocalDateTime.now());
        sessionInfo.setSessionExpiryTime(LocalDateTime.now().plusSeconds(session.getMaxInactiveInterval()));

        Set<String> permissions = new HashSet<>();
        if (user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
            permissions = user.getEmploymentDetail().getRole().getRolePermissions().stream()
                    .map(RolePermissionEntity::getPermission)
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());
        }

        sessionInfo.setPermissions(permissions);
        return sessionInfo;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @Override
    @Transactional
    public boolean changePassword(PasswordChangeRequestDto requestDto, HttpServletRequest request) {
        try {
            if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
                throw new IllegalArgumentException("Password và Confirm Password không khớp");
            }

            HttpSession session = request.getSession(false);
            if (session == null) {
                return false;
            }

            UUID userId = (UUID) session.getAttribute(SESSION_USER_KEY);
            if (userId == null) {
                return false;
            }

            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return false;
            }

            UserEntity user = userOptional.get();
            Optional<AccountEntity> accountOptional = accountRepository.findByUser(user);

            if (accountOptional.isEmpty()) {
                return false;
            }

            AccountEntity account = accountOptional.get();

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(requestDto.getCurrentPassword(), account.getPassword())) {
                return false;
            }

            // Mã hóa và lưu mật khẩu mới
            account.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

            // Nếu đang đổi mật khẩu mặc định, cập nhật trạng thái isDefaultPassword
            if (account.getIsDefaultPassword()) {
                account.setIsDefaultPassword(false);
            }

            accountRepository.save(account);

            return true;
        } catch (Exception e) {
            logger.error("Lỗi khi đổi mật khẩu: {}", e.getMessage());
            return false;
        }
    }
}
