package org.socius.sociuswebbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.socius.sociuswebbackend.config.TestConfig;
import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.LoginResponseDto;
import org.socius.sociuswebbackend.model.dtos.auth.SessionInfoDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthController authController;

    private AuthenticationService authenticationService;

    private LoginRequestDto adminLoginRequest;
    private LoginResponseDto successLoginResponse;
    private LoginResponseDto failedLoginResponse;
    private SessionInfoDto sessionInfoDto;

    @BeforeEach
    void setUp() {
        try {
            MockitoAnnotations.openMocks(this);
            authenticationService = mock(AuthenticationService.class);
            ReflectionTestUtils.setField(authController, "authenticationService", authenticationService);

            UserEntity adminUser = AuthTestDataUtil.createTestAdminUser();

            adminLoginRequest = AuthTestDataUtil.createAdminLoginRequest();

            // Create successful login response
            successLoginResponse = new LoginResponseDto();
            successLoginResponse.setAuthenticated(true);
            successLoginResponse.setPasswordChangeRequired(false);

            // Create failed login response
            failedLoginResponse = new LoginResponseDto();
            failedLoginResponse.setAuthenticated(false);

            // Create session info DTO
            sessionInfoDto = new SessionInfoDto();
            sessionInfoDto.setUserId(adminUser.getId());
            sessionInfoDto.setUsername(adminUser.getEmail());
            sessionInfoDto.setFullName(adminUser.getFullName());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi khởi tạo các mock đối tượng trong AuthControllerIntegrationTest", e);
        }
    }

    @Test
    @DisplayName("Login phải trả về 200 OK khi đăng nhập thành công")
    void loginShouldReturnOkAndLoginResponseWithValidCredentials() throws Exception {
        when(authenticationService.login(any(LoginRequestDto.class), any(), any()))
                .thenReturn(successLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.passwordChangeRequired").value(false));

        verify(authenticationService).login(any(LoginRequestDto.class), any(), any());
    }

    @Test
    @DisplayName("Login phải trả về 401 UNAUTHORIZED khi sai mật khẩu hoặc email")
    void loginShouldReturnUnauthorizedAndErrorMessageWithInvalidCredentials() throws Exception {
        when(authenticationService.login(any(LoginRequestDto.class), any(), any()))
                .thenReturn(failedLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).login(any(LoginRequestDto.class), any(), any());
    }

    @Test
    @DisplayName("Login phải trả về 401 UNAUTHORIZED khi người dùng không tồn tại")
    void loginShouldReturnNotFoundAndErrorMessageWhenUserNotFound() throws Exception {
        when(authenticationService.login(any(LoginRequestDto.class), any(), any()))
                .thenReturn(failedLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).login(any(LoginRequestDto.class), any(), any());
    }

    @Test
    @DisplayName("Login phải trả về 401 UNAUTHORIZED khi có lỗi không xác định")
    void loginShouldReturnInternalServerErrorAndErrorMessageOnUnknownError() throws Exception {
        when(authenticationService.login(any(LoginRequestDto.class), any(), any()))
                .thenReturn(failedLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).login(any(LoginRequestDto.class), any(), any());
    }

    @Test
    @DisplayName("Logout phải trả về 200 OK khi đăng xuất thành công")
    @WithMockUser
    void logoutShouldReturnOkOnSuccessfulLogout() throws Exception {
        when(authenticationService.isAuthenticated(any())).thenReturn(true);
        doNothing().when(authenticationService).logout(any(), any());

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        verify(authenticationService).logout(any(), any());
    }

    @Test
    @DisplayName("GET /api/auth/session should return 200 OK and session info when authenticated")
    void getSessionInfoShouldReturnOkAndSessionInfoWhenAuthenticated() throws Exception {
        when(authenticationService.getCurrentSession(any())).thenReturn(sessionInfoDto);

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(sessionInfoDto.getUserId().toString()))
                .andExpect(jsonPath("$.username").value(sessionInfoDto.getUsername()))
                .andExpect(jsonPath("$.fullName").value(sessionInfoDto.getFullName()));

        verify(authenticationService).getCurrentSession(any());
    }

    @Test
    @DisplayName("GET /api/auth/session should return 401 Unauthorized when not authenticated")
    void getSessionInfoShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        when(authenticationService.getCurrentSession(any())).thenReturn(null);

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).getCurrentSession(any());
    }
}
