package org.socius.sociuswebbackend.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.RoleMapper;
import org.socius.sociuswebbackend.mappers.UserMapper;
import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.AccountRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.*;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.websocket.WebSocketService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginHistoryService loginHistoryService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private RBACRedisService rbacRedisService;

    @Mock
    private ConfigService configService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private static ApplicationContext applicationContext;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private SessionManagementService sessionManagementService;

    @Mock
    private OnlineUserService onlineUserService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;


    private final String sessionId = "test-session-id";
    private final String sessionAttributeUserId = "session.attribute.user_id";


    private UserEntity adminUser;
    private AccountEntity adminAccount;
    private RoleEntity adminRole;
    private EmploymentDetailEntity adminEmploymentDetail;
    private UserResponseDto adminUserResponseDto;
    private LoginRequestDto adminLoginRequest;
    private UserPermissionsDto adminPermissionsDto;

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
        adminAccount = AuthTestDataUtil.createTestAdminAccount(adminUser);
        adminRole = AuthTestDataUtil.createTestAdminRole();
        adminEmploymentDetail = AuthTestDataUtil.createTestAdminEmploymentDetail(adminUser, adminRole);

        adminUserResponseDto = UserResponseDto.builder()
                .id(adminUser.getId())
                .email(adminUser.getEmail())
                .firstName(adminUser.getFirstName())
                .lastName(adminUser.getLastName())
                .build();

        adminLoginRequest = AuthTestDataUtil.createAdminLoginRequest();
        adminPermissionsDto = AuthTestDataUtil.createAdminPermissionsDto();

        adminUser.setAccount(adminAccount);
        adminUser.setEmploymentDetail(adminEmploymentDetail);

        try {
            Field contextField = ApplicationContextHelper.class.getDeclaredField("context");
            contextField.setAccessible(true);
            contextField.set(null, applicationContext);

            when(applicationContext.getBean(UserRepository.class)).thenReturn(userRepository);
            when(applicationContext.getBean(ConfigService.class)).thenReturn(configService);
            when(applicationContext.getBean(RoleMapper.class)).thenReturn(roleMapper);
            doNothing().when(onlineUserService).updateUserOnlineStatus(any(UUID.class), anyString());
        } catch (Exception e) {
            fail("Không thể thiết lập ApplicationContext: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Login phải phản hồi thành công khi thông tin đăng nhập hợp lệ")
    void loginShouldReturnSuccessResponseWhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(adminLoginRequest.getEmail())).thenReturn(Optional.of(adminUser));
        when(accountRepository.findByUser(adminUser)).thenReturn(Optional.of(adminAccount));
        when(userMapper.entityToDto(adminUser)).thenReturn(adminUserResponseDto);

        when(request.getSession(anyBoolean())).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);

        when(configService.getString(eq(sessionAttributeUserId), anyString())).thenReturn("USER_ID");
        when(configService.getInt(anyString(), anyInt())).thenReturn(30);

        LoginHistoryResponseDto loginHistoryResponseDto = new LoginHistoryResponseDto();
        when(loginHistoryService.createLoginHistory(any(LoginHistoryRequestDto.class)))
                .thenReturn(loginHistoryResponseDto);


        LoginResponseDto result = authenticationService.login(adminLoginRequest, request, response);

        assertNotNull(result, "Phản hồi không được null");
        assertTrue(result.isAuthenticated(), "Trạng thái xác thực phải true");
        assertEquals(adminUserResponseDto, result.getUser(), "Người dùng phải khớp");

        String userKey = configService.getString(sessionAttributeUserId, "USER_ID");
        verify(session).setAttribute(eq(userKey), eq(adminUser.getId()));
        verify(rbacRedisService).saveCacheUserPermissions(eq(sessionId), any(UserPermissionsDto.class), eq(30));
        verify(webSocketService).sendUserLoginNotification(anyString());
        verify(onlineUserService).updateUserOnlineStatus(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Login phải phẩn hồi thất bại khi thông tin đăng nhập không hợp lệ")
    void loginShouldReturnFailureResponseWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Thông tin đăng nhập không hợp lệ"));

        LoginResponseDto result = authenticationService.login(adminLoginRequest, request, response);

        assertNotNull(result, "Phản hồi không được null");
        assertNotNull(result.isAuthenticated(), "Trạng thái xác thực không được null");
        assertNull(result.getUser(), "Người dùng phải null");
        assertTrue(result.getMessage().contains("không hợp lệ"),
                "Thông báo phải chứa thông tin về đăng nhập không hợp lệ");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginHistoryService, never()).createLoginHistory(any(LoginHistoryRequestDto.class));
        verify(rbacRedisService, never()).saveCacheUserPermissions(anyString(), any(UserPermissionsDto.class),
                anyInt());
        verify(webSocketService, never()).sendUserLoginNotification(anyString());
    }

    @Test
    @DisplayName("Logout phải làm mất hiêu lực phiên đăng nhập và dọn dẹp redis cache")
    void logoutShouldInvalidateSessionAndCleanUpRedis() {
        when(request.getSession(false)).thenReturn(session);
        when(configService.getString(eq(sessionAttributeUserId), anyString())).thenReturn("USER_ID");
        when(session.getAttribute(anyString())).thenReturn(UUID.randomUUID());

        when(session.getId()).thenReturn(sessionId);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(sessionManagementService.invalidateSession(anyString())).thenReturn(true);

        authenticationService.logout(request, response);

        verify(rbacRedisService).deleteUserPermissions(sessionId);
        verify(session, atLeastOnce()).invalidate();
        verify(sessionManagementService).invalidateSession(sessionId);
    }

    @Test
    @DisplayName("Logout không thực hiện gì nếu không có phiên đăng nhập")
    void logoutShouldDoNothingWhenSessionIsNull() {
        when(request.getSession(false)).thenReturn(null);

        SessionInfoDto result = authenticationService.getCurrentSession(request);

        assertNull(result, "Phiên đăng nhập phải null");
    }

    @Test
    @DisplayName("Khi phiên và user tồn tại, xác thực thông tin phai trả về true")
    void isAuthenticatedShouldReturnTrueWhenSessionExistsAndUserIsAuthenticated() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("USER_ID")).thenReturn(adminUser.getId());
        when(configService.getString(anyString(), anyString())).thenReturn("USER_ID");

        boolean result = authenticationService.isAuthenticated(request);

        assertTrue(result, "Người dùng phải được xác thực");
    }

    @Test
    @DisplayName("Khi phiên không tồn tại, xác thực thông tin phai trả về false")
    void isAuthenticatedShouldReturnFalseWhenSessionDoesNotExist() {
        when(request.getSession(false)).thenReturn(null);

        boolean result = authenticationService.isAuthenticated(request);

        assertFalse(result, "Người dùng không được xác thực");
    }

    @Test
    @DisplayName("Khi phiên tồn tại thì gia hạn phiên phải trả về true")
    void extendSessionShouldReturnTrueWhenSessionExists() {
        when(request.getSession(false)).thenReturn(session);
        when(configService.getInt(eq("session.duration.minutes"), anyInt())).thenReturn(30);

        when(session.getId()).thenReturn(sessionId);

        when(rbacRedisService.extendExpiration(eq(sessionId), eq(30))).thenReturn(true);

        boolean result = authenticationService.extendSession(request);

        assertTrue(result, "Phiên đăng nhập phải được gia hạn");
        verify(session).setMaxInactiveInterval(1800);
        verify(rbacRedisService).extendExpiration(eq(sessionId), eq(30));
    }

    @Test
    @DisplayName("Khi phiên không tồn tại thì gia hạn phiên phải trả về false")
    void extendSessionShouldReturnFalseWhenSessionDoesNotExist() {
        when(request.getSession(false)).thenReturn(null);

        boolean result = authenticationService.extendSession(request);

        assertFalse(result, "Phiên đăng nhập không được gia hạn");
        verify(rbacRedisService, never()).extendExpiration(anyString(), anyInt());
    }

    @Test
    @DisplayName("Quy trình xác thực phải hoạt động đúng: đăng nhập, kiểm tra phiên, đăng xuất và hủy phiên")
    void authenticationFlowShouldProceedCorrectly() {
        // 1. Setup for login
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(adminLoginRequest.getEmail())).thenReturn(Optional.of(adminUser));
        when(accountRepository.findByUser(adminUser)).thenReturn(Optional.of(adminAccount));
        when(userMapper.entityToDto(adminUser)).thenReturn(adminUserResponseDto);

        when(request.getSession(anyBoolean())).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);

        when(configService.getString(eq(sessionAttributeUserId), anyString())).thenReturn("USER_ID");
        when(configService.getInt(anyString(), anyInt())).thenReturn(30);

        LoginHistoryResponseDto loginHistoryResponseDto = new LoginHistoryResponseDto();
        when(loginHistoryService.createLoginHistory(any(LoginHistoryRequestDto.class)))
                .thenReturn(loginHistoryResponseDto);

        // 2. Perform login
        LoginResponseDto loginResult = authenticationService.login(adminLoginRequest, request, response);

        // 3. Verify login was successful
        assertNotNull(loginResult, "Phản hồi đăng nhập không được null");
        assertTrue(loginResult.isAuthenticated(), "Xác thực phải thành công");
        assertEquals(adminUserResponseDto, loginResult.getUser(), "Thông tin người dùng phải chính xác");
        assertFalse(loginResult.isPasswordChangeRequired(), "Không yêu cầu đổi mật khẩu");

        // 4. Additional setup for session info retrieval
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(adminUser));
        when(session.getAttribute("USER_ID")).thenReturn(adminUser.getId());
        when(request.getSession(false)).thenReturn(session);

        // 5. Get session info and verify
        SessionInfoDto sessionInfo = authenticationService.getCurrentSession(request);

        // 6. Verify session info
        assertNotNull(sessionInfo, "Thông tin phiên không được null");
        assertEquals(adminUser.getId(), sessionInfo.getUserId(), "ID người dùng phải khớp");
        assertEquals(adminUser.getEmail(), sessionInfo.getEmail(), "Email phải khớp");
        assertEquals(sessionId, sessionInfo.getSessionId(), "ID phiên phải khớp");

        // 7. Setup for logout
        doNothing().when(session).invalidate();

        // 8. Perform logout
        authenticationService.logout(request, response);

        // 9. Verify logout called session invalidate
        verify(session, times(1)).invalidate();

        // 10. Setup for checking session after logout
        when(request.getSession(false)).thenReturn(null); // Session now null after logout

        // 11. Verify session is invalidated after logout
        SessionInfoDto sessionInfoAfterLogout = authenticationService.getCurrentSession(request);
        assertNull(sessionInfoAfterLogout, "Thông tin phiên phải null sau khi đăng xuất");

        // 12. Verify user is no longer authenticated
        assertFalse(authenticationService.isAuthenticated(request),
                "Người dùng không được xác thực sau khi đăng xuất");
    }
}
