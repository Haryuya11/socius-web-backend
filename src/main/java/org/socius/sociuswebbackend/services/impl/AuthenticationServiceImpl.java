package org.socius.sociuswebbackend.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.RoleMapper;
import org.socius.sociuswebbackend.mappers.UserMapper;
import org.socius.sociuswebbackend.model.dtos.auth.*;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.PasswordChangeResult;
import org.socius.sociuswebbackend.repositories.AccountRepository;
import org.socius.sociuswebbackend.repositories.RoleRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.*;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    final private AuthenticationManager authenticationManager;
    final private UserRepository userRepository;
    final private RoleRepository roleRepository;
    final private AccountRepository accountRepository;
    final private UserMapper userMapper;
    final private RoleMapper roleMapper;
    final private LoginHistoryService loginHistoryService;
    final private WebSocketService webSocketService;
    final private PasswordEncoder passwordEncoder;
    final private RBACRedisService rbacRedisService;
    final private ConfigService configService;
    final private OnlineUserService onlineUserService;
    final private SessionManagementService sessionManagementService;

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
            String userKey = configService.getString("session.attribute.user_id", "USER_ID");
            String teamKey = configService.getString("session.attribute.team_id", "TEAM_ID");
            String roleKey = configService.getString("session.attribute.role_id", "ROLE_ID");

            session.setAttribute(userKey, user.getId());
            if (user.getEmploymentDetail() != null) {
                EmploymentDetailEntity employmentDetail = user.getEmploymentDetail();

                if (employmentDetail.getTeam() != null) {
                    session.setAttribute(teamKey, employmentDetail.getTeam().getId());
                }

                if (employmentDetail.getRole() != null) {
                    session.setAttribute(roleKey, employmentDetail.getRole().getId());
                }
            }

            // 8. Lấy danh sách các quyền của người dùng
            Set<String> permissions = new HashSet<>();
            String roleName = null;
            UUID roleId = null;
            if (user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
                RoleEntity role = user.getEmploymentDetail().getRole();
                roleName = role.getName();
                roleId = role.getId();

                permissions = user.getEmploymentDetail().getRole().getRolePermissions().stream()
                        .filter(rp -> rp.getPermission() != null)
                        .map(rp -> rp.getPermission().getName())
                        .collect(Collectors.toSet());
            }

            // 9. Lưu thông tin quyền vào Redis
            String sessionId = session.getId();
            UserPermissionsDto permissionsDto = UserPermissionsDto.builder()
                    .userId(user.getId())
                    .roleId(roleId)
                    .roleName(roleName)
                    .permissions(permissions)
                    .build();
            int sessionDurationMinutes = configService.getInt("session.duration.minutes", 30);
            rbacRedisService.saveCacheUserPermissions(sessionId, permissionsDto, sessionDurationMinutes);

            // 10. Lưu thông tin lịch sử đăng nhập
            String ipAddress = getClientIp(request);
            String deviceInfo = request.getHeader("User-Agent");

            LoginHistoryRequestDto historyDto = LoginHistoryRequestDto.builder()
                    .userId(user.getId())
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .build();

            loginHistoryService.createLoginHistory(historyDto);

            // 11. Gửi thông báo đến WebSocket
            webSocketService.sendUserLoginNotification(user.getFirstName() + " " + user.getLastName());

            // Cập nhật trạng thái online của người dùng
            onlineUserService.updateUserOnlineStatus(user.getId(), session.getId());

            UserResponseDto userDto = userMapper.entityToDto(user);

            if (roleId != null && user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
                RoleEntity fullRoleEntity = roleRepository.findById(roleId).orElse(null);
                if (fullRoleEntity != null) {
                    RoleResponseDto completeRoleDto = roleMapper.entityToDto(fullRoleEntity);
                    userDto.setRole(completeRoleDto);
                }
            }

            // 12. Tạo đối tượng phản hồi
            responseDto.setUser(userDto);
            responseDto.setAuthenticated(true);
            responseDto.setSessionId(session.getId());
            responseDto.setMessage("Đăng nhập thành công");
            return responseDto;

        } catch (AuthenticationException e) {
            logger.error("Lỗi khi đăng nhập: {}", e.getMessage());
            responseDto.setAuthenticated(false);
            if (e.getMessage().contains("Bad credentials")) {
                responseDto.setMessage("Sai mật khẩu");
            } else {
                responseDto.setMessage("Lỗi hệ thống");
            }
            return responseDto;
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        if (!isAuthenticated(request)) {
            logger.warn("Người dùng chưa đăng nhập, không thể đăng xuất");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            String userKey = configService.getString("session.attribute.user_id", "USER_ID");
            UUID userId = (UUID) session.getAttribute(userKey);
            if (userId != null) {
                onlineUserService.markUserOffline(userId, sessionId);
            }

            // Xóa thông tin quyền của người dùng khỏi Redis
            rbacRedisService.deleteUserPermissions(sessionId);

            // Xóa session trong Redis
            sessionManagementService.invalidateSession(sessionId);

            // Gửi thông báo đến WebSocket
            webSocketService.sendUserLogoutNotification(userKey);


            // Đăng xuất và hủy phiên
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logoutHandler.logout(request, response, authentication);

            logger.info("Người dùng đã đăng xuất: {}", sessionId);
        }
    }

    @Override
    public SessionInfoDto getCurrentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String userKey = configService.getString("session.attribute.user_id", "USER_ID");
        UUID userId = (UUID) session.getAttribute(userKey);
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

        if (user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
            RoleEntity roleEntity = user.getEmploymentDetail().getRole();
            RoleMapper roleMapper = ApplicationContextHelper.getBean(RoleMapper.class);
            RoleResponseDto roleDto = roleMapper.entityToDto(roleEntity);
            sessionInfo.setRole(roleDto);
        }

        return sessionInfo;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String userKey = configService.getString("session.attribute.user_id", "USER_ID");

        return session != null && session.getAttribute(userKey) != null;
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
    public PasswordChangeResult changePassword(PasswordChangeRequestDto requestDto, HttpServletRequest request) {
        try {
            if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
                throw new IllegalArgumentException("Password và Confirm Password không khớp");
            }

            HttpSession session = request.getSession(false);
            if (session == null) {
//                throw new IllegalStateException("Chưa đăng nhập");
                return PasswordChangeResult.NOT_AUTHENTICATED;
            }

            // Lấy thông tin người dùng từ session
            String userKey = configService.getString("session.attribute.user_id", "USER_ID");
            UUID userId = (UUID) session.getAttribute(userKey);
            if (userId == null) {
                return PasswordChangeResult.NOT_AUTHENTICATED;
            }

            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return PasswordChangeResult.USER_NOT_FOUND;
            }

            UserEntity user = userOptional.get();
            Optional<AccountEntity> accountOptional = accountRepository.findByUser(user);

            if (accountOptional.isEmpty()) {
                return PasswordChangeResult.ACCOUNT_NOT_FOUND;
            }

            AccountEntity account = accountOptional.get();

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(requestDto.getCurrentPassword(), account.getPassword())) {
                return PasswordChangeResult.INCORRECT_PASSWORD;
            }

            // Mã hóa và lưu mật khẩu mới
            account.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

            // Nếu đang đổi mật khẩu mặc định, cập nhật trạng thái isDefaultPassword
            if (account.getIsDefaultPassword()) {
                account.setIsDefaultPassword(false);
            }

            accountRepository.save(account);

            return PasswordChangeResult.SUCCESS;
        } catch (Exception e) {
            logger.error("Lỗi khi đổi mật khẩu: {}", e.getMessage());
            return PasswordChangeResult.GENERAL_ERROR;
        }
    }

    @Override
    public UserPermissionsDto getCurrentUserPermissions(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        // Lấy thông tin quyền từ Redis
        UserPermissionsDto permissions = rbacRedisService.getUserPermissions(session.getId());

        // Nếu không có trong Redis, lấy từ database
        if (permissions == null) {
            // Lấy thông tin người dùng
            String userKey = configService.getString("session.attribute.user_id", "USER_ID");
            UUID userId = (UUID) session.getAttribute(userKey);
            if (userId == null) {
                return null;
            }
            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty() || userOptional.get().getEmploymentDetail() == null
                    || userOptional.get().getEmploymentDetail().getRole() == null) {
                return null;
            }
            UserEntity user = userOptional.get();
            RoleEntity role = user.getEmploymentDetail().getRole();

            // Lấy thông tin quyền từ database
            permissions = new UserPermissionsDto();
            permissions.setUserId(user.getId());
            permissions.setRoleId(role.getId());
            permissions.setRoleName(role.getName());

            Set<String> permissionsSet = role.getRolePermissions().stream()
                    .filter(rp -> rp.getPermission() != null)
                    .map(rp -> rp.getPermission().getName())
                    .collect(Collectors.toSet());
            permissions.setPermissions(permissionsSet);

            // Lưu thông tin quyền vào Redis
            int sessionDurationMinutes = configService.getInt("session.duration.minutes", 30);
            rbacRedisService.saveCacheUserPermissions(session.getId(), permissions, sessionDurationMinutes);
        }
        return permissions;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, String permission) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        // Kiểm tra quyền trong Redis
        boolean hasPermission = rbacRedisService.hasPermission(session.getId(), permission);

        // Nếu không có trong Redis, kiểm tra trong database
        if (!hasPermission) {
            UserPermissionsDto permissions = getCurrentUserPermissions(request);
            if (permissions != null && permissions.getPermissions() != null) {
                hasPermission = permissions.getPermissions().contains(permission);
            }
        }
        return hasPermission;
    }

    @Override
    public boolean extendSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        // Gia hạn thời gian hết hạn của phiên
        int sessionDurationMinutes = configService.getInt("session.duration.minutes", 30);
        boolean result = rbacRedisService.extendExpiration(session.getId(), sessionDurationMinutes);
        if (result) {
            session.setMaxInactiveInterval(sessionDurationMinutes * 60);
        } else {
            logger.error("Lỗi khi gia hạn phiên làm việc");
        }
        return result;
    }
}
