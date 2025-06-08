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
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmploymentDetailServiceImplTest {

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private EmploymentDetailMapper employmentDetailMapper;

    @InjectMocks
    private EmploymentDetailServiceImpl employmentDetailService;

    private EmploymentDetailEntity testEmploymentDetail;
    private EmploymentDetailResponseDto testEmploymentDetailResponseDto;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        UserEntity testUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("employee@example.com")
                .firstName("Nguyễn")
                .lastName("Văn Bình")
                .build();

        testEmploymentDetail = EmploymentDetailEntity.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .startDate(LocalDate.of(2023, 1, 1))
                .salary(new BigDecimal("5000.00"))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        UserResponseDto testUserResponseDto = UserResponseDto.builder()
                .id(testUser.getId())
                .firstName("Nguyễn")
                .lastName("Văn Bình")
                .build();

        testEmploymentDetailResponseDto = EmploymentDetailResponseDto.builder()
                .id(testEmploymentDetail.getId())
                .user(testUserResponseDto)
                .startDate(LocalDate.of(2023, 1, 1))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Lấy danh sách nhân viên phải gọi service và trả về dữ liệu phân trang")
    void getAllEmployeesShouldCallService() {
        // Arrange
        Page<EmploymentDetailEntity> employeePage = new PageImpl<>(Collections.singletonList(testEmploymentDetail), testPageable, 1);
        when(employmentDetailRepository.findAll(testPageable)).thenReturn(employeePage);
        when(employmentDetailMapper.entityToLimitedDto(testEmploymentDetail)).thenReturn(testEmploymentDetailResponseDto);

        // Act
        Map<String, Object> result = employmentDetailService.getAllActiveEmployees(testPageable);

        // Assert
        verify(employmentDetailRepository, times(1)).findAll(testPageable);
        verify(employmentDetailMapper, times(1)).entityToLimitedDto(testEmploymentDetail);
        assertEquals(1, ((List<?>) result.get("employees")).size());
        assertEquals(1, result.get("employeeCount"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));
    }

    @Test
    @DisplayName("Lấy danh sách nhân viên cho admin phải gọi service và trả về dữ liệu phân trang")
    void getAllEmployeesForAdminShouldCallService() {
        // Arrange
        Page<EmploymentDetailEntity> employeePage = new PageImpl<>(Collections.singletonList(testEmploymentDetail), testPageable, 1);
        when(employmentDetailRepository.findAll(testPageable)).thenReturn(employeePage);
        when(employmentDetailMapper.entityToLimitedDtoForAdmin(testEmploymentDetail)).thenReturn(testEmploymentDetailResponseDto);

        // Act
        Map<String, Object> result = employmentDetailService.getAllActiveEmployeesForAdmin(testPageable);

        // Assert
        verify(employmentDetailRepository, times(1)).findAll(testPageable);
        verify(employmentDetailMapper, times(1)).entityToLimitedDtoForAdmin(testEmploymentDetail);
        assertEquals(1, ((List<?>) result.get("employees")).size());
        assertEquals(1, result.get("employeeCount"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));
    }
}