package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.LoginHistoryMapper;
import org.socius.sociuswebbackend.model.dtos.loginHistory.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.loginHistory.LoginHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.LoginHistoryEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.LoginHistoryRepository;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoginHistoryServiceImplTest {
    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private LoginHistoryMapper loginHistoryMapper;

    @InjectMocks
    private LoginHistoryServiceImpl loginHistoryService;

    private UserEntity adminUser;
    private LoginHistoryResponseDto loginHistoryResponseDto;
    private LoginHistoryRequestDto loginHistoryRequestDto;
    private LoginHistoryEntity loginHistoryEntity;

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
        loginHistoryRequestDto = AuthTestDataUtil.createAdminLoginHistoryRequest();
        loginHistoryEntity = AuthTestDataUtil.createAdminLoginHistory(adminUser);
        loginHistoryResponseDto = new LoginHistoryResponseDto();
        loginHistoryResponseDto.setId(loginHistoryEntity.getId());
    }

    @Test
    @DisplayName("Create login history should save entity and return DTO")
    void createLoginHistoryShouldSaveEntityAndReturnDto(){
        when(loginHistoryMapper.requestDtoToEntity(loginHistoryRequestDto)).thenReturn(loginHistoryEntity);
        when(loginHistoryRepository.save(loginHistoryEntity)).thenReturn(loginHistoryEntity);
        when(loginHistoryMapper.entityToDto(loginHistoryEntity)).thenReturn(loginHistoryResponseDto);

        LoginHistoryResponseDto result = loginHistoryService.createLoginHistory(loginHistoryRequestDto);

        assertNotNull(result,"Kết quả không được null");
        assertEquals(loginHistoryResponseDto, result, "Kết quả không khớp với mong đợi");

        verify(loginHistoryMapper).requestDtoToEntity(loginHistoryRequestDto);
        verify(loginHistoryRepository).save(loginHistoryEntity);
        verify(loginHistoryMapper).entityToDto(loginHistoryEntity);
    }

    @Test
    @DisplayName("Get login history by user ID should return list of histories")
    void getLoginHistoryByUserIdShouldReturnListOfHistories() {
        UUID userId = adminUser.getId();
        List<LoginHistoryEntity> entities = Collections.singletonList(loginHistoryEntity);
        List<LoginHistoryResponseDto> dtos = Collections.singletonList(loginHistoryResponseDto);

        when(loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId)).thenReturn(entities);
        when(loginHistoryMapper.entitiesToDtos(entities)).thenReturn(dtos);

        List<LoginHistoryResponseDto> result = loginHistoryService.getLoginHistoryByUserId(userId);

        assertNotNull(result, "Kết quả không được null");
        assertEquals(1, result.size(), "Phải trả về 1 lịch sử đăng nhập");
        assertEquals(loginHistoryResponseDto, result.get(0), "DTO không khớp với mong đợi");

        verify(loginHistoryRepository).findByUserIdOrderByLoginTimeDesc(userId);
        verify(loginHistoryMapper).entitiesToDtos(entities);
    }
}
