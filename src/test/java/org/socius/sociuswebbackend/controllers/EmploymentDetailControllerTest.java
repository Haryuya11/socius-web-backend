package org.socius.sociuswebbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmploymentDetailControllerTest {

    @Mock
    private EmploymentDetailService employmentDetailService;

    @InjectMocks
    private EmploymentDetailController employmentDetailController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserResponseDto testUserResponseDto;
    private EmploymentDetailResponseDto testEmploymentDetailResponseDto;
    private Map<String, Object> testResponseMap;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Thêm PageableHandlerMethodArgumentResolver
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(employmentDetailController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        testUserResponseDto = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .firstName("Nguyễn")
                .lastName("Văn Bình")
                .build();

        testEmploymentDetailResponseDto = EmploymentDetailResponseDto.builder()
                .id(UUID.randomUUID())
                .user(testUserResponseDto)
                .startDate(LocalDate.of(2023, 1, 1))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        testResponseMap = new HashMap<>();
        testResponseMap.put("employees", Arrays.asList(testEmploymentDetailResponseDto));
        testResponseMap.put("employeeCount", 1);
        testResponseMap.put("totalPages", 1);
        testResponseMap.put("totalElements", 1L);

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Lấy danh sách nhân viên phải gọi service")
    void getAllEmployeesShouldCallService() throws Exception {
        // Arrange
        when(employmentDetailService.getAllActiveEmployees(testPageable)).thenReturn(testResponseMap); // Sửa method name

        // Act
        mockMvc.perform(get("/api/employee/all")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCount").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        // Assert
        verify(employmentDetailService, times(1)).getAllActiveEmployees(testPageable);
    }

    @Test
    @DisplayName("Lấy danh sách nhân viên cho admin phải gọi service")
    @WithMockUser(authorities = "ACCESS_ADMIN_PAGE")
    void getAllEmployeesForAdminShouldCallService() throws Exception {
        // Arrange
        when(employmentDetailService.getAllActiveEmployeesForAdmin(testPageable)).thenReturn(testResponseMap);

        // Act
        mockMvc.perform(get("/api/employee/admin/all")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCount").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        // Assert
        verify(employmentDetailService, times(1)).getAllActiveEmployeesForAdmin(testPageable);
    }
}