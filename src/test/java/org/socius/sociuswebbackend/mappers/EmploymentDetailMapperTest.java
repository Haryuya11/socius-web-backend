package org.socius.sociuswebbackend.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmploymentDetailMapperTest {

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private EmploymentDetailMapperImpl employmentDetailMapper;

    private UserEntity testUser;
    private EmploymentDetailEntity testEmploymentDetail;
    private UserResponseDto testUserResponseDto;
    private EmploymentDetailResponseDto testEmploymentDetailResponseDto;
    private EmploymentDetailRequestDto testEmploymentDetailRequestDto;
    private PositionEntity testPosition;
    private DepartmentEntity testDepartment;
    private TeamEntity testTeam;
    private RoleEntity testRole;
    private PositionResponseDto testPositionResponseDto;
    private DepartmentResponseDto testDepartmentResponseDto;
    private TeamResponseDto testTeamResponseDto;
    private RoleResponseDto testRoleResponseDto;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("employee@example.com")
                .firstName("Nguyễn")
                .lastName("Văn Bình")
                .build();

        testPosition = PositionEntity.builder()
                .id(UUID.randomUUID())
                .name("Developer")
                .description("Software Developer")
                .build();

        testDepartment = DepartmentEntity.builder()
                .id(UUID.randomUUID())
                .name("IT")
                .description("Information Technology")
                .build();

        testTeam = TeamEntity.builder()
                .id(UUID.randomUUID())
                .name("Backend Team")
                .build();

        testRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .name("Employee")
                .description("Standard Employee Role")
                .build();

        testEmploymentDetail = EmploymentDetailEntity.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .position(testPosition)
                .department(testDepartment)
                .team(testTeam)
                .role(testRole)
                .startDate(LocalDate.of(2023, 1, 1))
                .salary(new BigDecimal("5000.00"))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        testUserResponseDto = UserResponseDto.builder()
                .id(testUser.getId())
                .firstName("Nguyễn")
                .lastName("Văn Bình")
                .build();

        testPositionResponseDto = PositionResponseDto.builder()
                .id(testPosition.getId())
                .name("Developer")
                .description("Software Developer")
                .build();

        testDepartmentResponseDto = DepartmentResponseDto.builder()
                .id(testDepartment.getId())
                .name("IT")
                .description("Information Technology")
                .build();

        testTeamResponseDto = TeamResponseDto.builder()
                .id(testTeam.getId())
                .name("Backend Team")
                .build();

        testRoleResponseDto = RoleResponseDto.builder()
                .id(testRole.getId())
                .name("Employee")
                .description("Standard Employee Role")
                .build();

        testEmploymentDetailResponseDto = EmploymentDetailResponseDto.builder()
                .id(testEmploymentDetail.getId())
                .user(testUserResponseDto)
                .position(testPositionResponseDto)
                .department(testDepartmentResponseDto)
                .team(testTeamResponseDto)
                .role(testRoleResponseDto)
                .startDate(LocalDate.of(2023, 1, 1))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        testEmploymentDetailRequestDto = EmploymentDetailRequestDto.builder()
                .userId(testUser.getId())
                .positionId(testPosition.getId())
                .departmentId(testDepartment.getId())
                .teamId(testTeam.getId())
                .roleId(testRole.getId())
                .startDate(LocalDate.of(2023, 1, 1))
                .salary(new BigDecimal("5000.00"))
                .workingStatus(WorkingStatus.valueOf("active"))
                .build();

        // Mock EntityMappingUtil
        when(entityMappingUtil.mapUserIdToEntity(testUser.getId())).thenReturn(testUser);
        when(entityMappingUtil.mapPositionIdToEntity(testPosition.getId())).thenReturn(testPosition);
        when(entityMappingUtil.mapDepartmentIdToEntity(testDepartment.getId())).thenReturn(testDepartment);
        when(entityMappingUtil.mapTeamIdToEntity(testTeam.getId())).thenReturn(testTeam);
        when(entityMappingUtil.mapRoleIdToEntity(testRole.getId())).thenReturn(testRole);
        // Mock Mappers
        when(userMapper.toLimitedDto(testUser)).thenReturn(testUserResponseDto);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);
        when(departmentMapper.entityToDto(testDepartment)).thenReturn(testDepartmentResponseDto);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);
    }

    @Test
    @DisplayName("Ánh xạ entity sang limited DTO phải trả về DTO đúng")
    void entityToLimitedDtoShouldReturnCorrectDto() {
        // Arrange
        when(userMapper.toLimitedDto(testUser)).thenReturn(testUserResponseDto);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);
        when(departmentMapper.entityToDto(testDepartment)).thenReturn(testDepartmentResponseDto);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);

        // Act
        EmploymentDetailResponseDto result = employmentDetailMapper.entityToLimitedDto(testEmploymentDetail);

        // Assert
        verify(userMapper, times(1)).toLimitedDto(testUser);
        verify(positionMapper, times(1)).entityToDto(testPosition);
        verify(departmentMapper, times(1)).entityToDto(testDepartment);
        verify(teamMapper, times(1)).entityToDto(testTeam);
        verifyNoInteractions(roleMapper);
        assertNotNull(result);
        assertEquals(testEmploymentDetail.getId(), result.getId());
        assertEquals(testUserResponseDto, result.getUser());
        assertEquals(testPositionResponseDto, result.getPosition());
        assertEquals(testDepartmentResponseDto, result.getDepartment());
        assertEquals(testTeamResponseDto, result.getTeam());
        assertNull(result.getRole());
        assertEquals(testEmploymentDetail.getStartDate(), result.getStartDate());
        assertEquals(testEmploymentDetail.getWorkingStatus(), result.getWorkingStatus());
        assertNull(result.getSalary());
    }

    @Test
    @DisplayName("Ánh xạ entity sang limited DTO cho admin phải trả về DTO đúng")
    void entityToLimitedDtoForAdminShouldReturnCorrectDto() {
        // Arrange
        when(userMapper.toLimitedDto(testUser)).thenReturn(testUserResponseDto);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);
        when(departmentMapper.entityToDto(testDepartment)).thenReturn(testDepartmentResponseDto);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // Act
        EmploymentDetailResponseDto result = employmentDetailMapper.entityToLimitedDtoForAdmin(testEmploymentDetail);

        // Assert
        verify(userMapper, times(1)).toLimitedDto(testUser);
        verify(positionMapper, times(1)).entityToDto(testPosition);
        verify(departmentMapper, times(1)).entityToDto(testDepartment);
        verify(teamMapper, times(1)).entityToDto(testTeam);
        verify(roleMapper, times(1)).entityToDto(testRole);
        assertNotNull(result);
        assertEquals(testEmploymentDetail.getId(), result.getId());
        assertEquals(testUserResponseDto, result.getUser());
        assertEquals(testPositionResponseDto, result.getPosition());
        assertEquals(testDepartmentResponseDto, result.getDepartment());
        assertEquals(testTeamResponseDto, result.getTeam());
        assertEquals(testRoleResponseDto, result.getRole());
        assertEquals(testEmploymentDetail.getStartDate(), result.getStartDate());
        assertEquals(testEmploymentDetail.getWorkingStatus(), result.getWorkingStatus());
        assertNull(result.getSalary());
    }

    @Test
    @DisplayName("Ánh xạ request DTO sang entity phải trả về entity đúng")
    void requestDtoToEntityShouldReturnCorrectEntity() {
        // Act
        EmploymentDetailEntity result = employmentDetailMapper.requestDtoToEntity(testEmploymentDetailRequestDto);

        // Assert
        verify(entityMappingUtil, times(1)).mapUserIdToEntity(testUser.getId());
        verify(entityMappingUtil, times(1)).mapPositionIdToEntity(testPosition.getId());
        verify(entityMappingUtil, times(1)).mapDepartmentIdToEntity(testDepartment.getId());
        verify(entityMappingUtil, times(1)).mapTeamIdToEntity(testTeam.getId());
        verify(entityMappingUtil, times(1)).mapRoleIdToEntity(testRole.getId());
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testPosition, result.getPosition());
        assertEquals(testDepartment, result.getDepartment());
        assertEquals(testTeam, result.getTeam());
        assertEquals(testRole, result.getRole());
        assertEquals(testEmploymentDetailRequestDto.getStartDate(), result.getStartDate());
        assertEquals(testEmploymentDetailRequestDto.getSalary(), result.getSalary());
        assertEquals(testEmploymentDetailRequestDto.getWorkingStatus(), result.getWorkingStatus());
    }

    @Test
    @DisplayName("Cập nhật entity từ request DTO phải cập nhật các trường đúng")
    void updateEntityFromDtoShouldCall() {
        // Arrange
        EmploymentDetailEntity entityToUpdate = EmploymentDetailEntity.builder()
                .id(UUID.randomUUID())
                .build();

        // Act
        employmentDetailMapper.updateEntityFromDto(testEmploymentDetailRequestDto, entityToUpdate);

        // Assert
        verify(entityMappingUtil, times(1)).mapUserIdToEntity(testUser.getId());
        verify(entityMappingUtil, times(1)).mapPositionIdToEntity(testPosition.getId());
        verify(entityMappingUtil, times(1)).mapDepartmentIdToEntity(testDepartment.getId());
        verify(entityMappingUtil, times(1)).mapTeamIdToEntity(testTeam.getId());
        verify(entityMappingUtil, times(1)).mapRoleIdToEntity(testRole.getId());
        assertEquals(testUser, entityToUpdate.getUser());
        assertEquals(testPosition, entityToUpdate.getPosition());
        assertEquals(testDepartment, entityToUpdate.getDepartment());
        assertEquals(testTeam, entityToUpdate.getTeam());
        assertEquals(testRole, entityToUpdate.getRole());
        assertEquals(testEmploymentDetailRequestDto.getStartDate(), entityToUpdate.getStartDate());
        assertEquals(testEmploymentDetailRequestDto.getSalary(), entityToUpdate.getSalary());
        assertEquals(testEmploymentDetailRequestDto.getWorkingStatus(), entityToUpdate.getWorkingStatus());
    }
}