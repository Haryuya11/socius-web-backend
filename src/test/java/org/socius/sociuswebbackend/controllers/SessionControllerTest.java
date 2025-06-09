package org.socius.sociuswebbackend.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionControllerTest {
    @Mock
    private OnlineUserService onlineUserService;

    @InjectMocks
    private SessionController sessionController;

    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
    }

    @Test
    @DisplayName("checkUserStatus phải trả về true khi người dùng đang online")
    void checkUserStatusShouldReturnTrueWhenUserIsOnline() {
        when(onlineUserService.isUserOnline(adminUser.getId())).thenReturn(true);

        ResponseEntity<Boolean> response = sessionController.checkUserStatus(adminUser.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }
}
