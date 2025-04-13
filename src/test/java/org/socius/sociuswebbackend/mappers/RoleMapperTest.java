package org.socius.sociuswebbackend.mappers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionId;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Lớp kiểm thử cho RoleMapper.
 * 
 * Các bài kiểm tra này xác minh chức năng ánh xạ giữa các thực thể Role và các DTO,
 * với trọng tâm đặc biệt vào việc xử lý các mối quan hệ permission.
 * 
 * Lớp kiểm thử này sử dụng MockedStatic để giả lập các phương thức tĩnh trong ApplicationContextHelper,
 * điều này là cần thiết để kiểm tra cơ chế giải quyết phụ thuộc của mapper.
 */
@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    @Mock
    private EntityMappingUtil entityMappingUtil;
    
    @Mock
    private PermissionMapper permissionMapper;
    
    @InjectMocks
    private RoleMapperImpl roleMapper;
    
    // Khai báo mock tĩnh ở cấp lớp
    private MockedStatic<ApplicationContextHelper> mockedStatic;

    /**
     * Phương thức thiết lập được thực hiện trước mỗi bài kiểm tra.
     * 
     * Phương thức này cấu hình các mock tĩnh cần thiết để mô phỏng ngữ cảnh Spring
     * và quá trình tiêm phụ thuộc mà mapper dựa vào.
     */
    @BeforeEach
    void setUp() {
        // Khởi tạo mock tĩnh cho ApplicationContextHelper
        mockedStatic = mockStatic(ApplicationContextHelper.class);
        
        // Cấu hình mock trả về các bean giả lập của chúng ta khi được yêu cầu
        mockedStatic.when(() -> ApplicationContextHelper.getBean(PermissionMapper.class))
                  .thenReturn(permissionMapper);
        mockedStatic.when(() -> ApplicationContextHelper.getBean(EntityMappingUtil.class))
                  .thenReturn(entityMappingUtil);
    }
    
    /**
     * Phương thức dọn dẹp thực hiện sau mỗi bài kiểm tra.
     * 
     * Đóng đúng cách mock tĩnh để ngăn rò rỉ bộ nhớ và nhiễu kiểm tra.
     */
    @AfterEach
    void tearDown() {
        // Đóng mock sau mỗi bài kiểm tra để ngăn rò rỉ
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    /**
     * Kiểm tra chức năng ánh xạ từ thực thể sang DTO cho vai trò có quyền.
     * 
     * Bài kiểm tra này xác minh rằng mapper đúng ánh xạ một RoleEntity có các quyền liên kết
     * thành một RoleResponseDto, giữ nguyên tất cả dữ liệu liên quan.
     * 
     * Đầu vào:
     * - Một RoleEntity với ID, tên, mô tả, và hai liên kết quyền
     * 
     * Kết quả mong đợi:
     * - Một RoleResponseDto có cùng ID, tên và mô tả
     * - Các quyền sẽ được ánh xạ bởi phương thức afterMapping (không được xác minh rõ ràng trong bài kiểm tra này)
     */
    @Test
    @DisplayName("Should map entity to DTO with permissions")
    void shouldMapEntityToDtoWithPermissions() {
        // Khởi tạo - Tạo vai trò thử nghiệm với các quyền
        UUID roleId = UUID.randomUUID();
        UUID permId1 = UUID.randomUUID();
        UUID permId2 = UUID.randomUUID();
        
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        role.setName("ADMIN");
        role.setDescription("Administrator role");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        
        Set<RolePermissionEntity> permissions = new HashSet<>();
        
        PermissionEntity perm1 = new PermissionEntity();
        perm1.setId(permId1);
        perm1.setName("CREATE_USER");
        
        PermissionEntity perm2 = new PermissionEntity();
        perm2.setId(permId2);
        perm2.setName("DELETE_USER");
        
        RolePermissionEntity rp1 = new RolePermissionEntity();
        rp1.setId(new RolePermissionId(roleId, permId1));
        rp1.setRole(role);
        rp1.setPermission(perm1);
        
        RolePermissionEntity rp2 = new RolePermissionEntity();
        rp2.setId(new RolePermissionId(roleId, permId2));
        rp2.setRole(role);
        rp2.setPermission(perm2);
        
        permissions.add(rp1);
        permissions.add(rp2);
        role.setRolePermissions(permissions);
        
        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        RoleResponseDto dto = roleMapper.entityToDto(role);
        
        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(roleId, dto.getId());
        assertEquals("ADMIN", dto.getName());
        assertEquals("Administrator role", dto.getDescription());
        // Các quyền sẽ được ánh xạ bởi phương thức afterMapping
    }
    
    /**
     * Kiểm tra chức năng cập nhật thực thể từ DTO với quyền.
     * 
     * Bài kiểm tra này xác minh rằng mapper đúng cập nhật một RoleEntity từ một RoleRequestDto,
     * đặc biệt tập trung vào cách xử lý các liên kết quyền.
     * 
     * Đầu vào:
     * - Một RoleEntity với trạng thái ban đầu
     * - Một RoleRequestDto với các giá trị đã cập nhật và ID quyền
     * 
     * Kết quả mong đợi:
     * - RoleEntity được cập nhật với giá trị từ DTO
     * - Các quyền của vai trò được tạo lại chính xác dựa trên ID quyền trong DTO
     */
    @Test
    @DisplayName("Should update entity permissions from DTO")
    void shouldUpdateEntityPermissionsFromDto() {
        // Khởi tạo - Tạo vai trò thử nghiệm và DTO với các quyền
        UUID roleId = UUID.randomUUID();
        UUID permId1 = UUID.randomUUID();
        UUID permId2 = UUID.randomUUID();
        
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        role.setName("EDITOR");
        role.setDescription("Editor role");
        role.setRolePermissions(new HashSet<>());
        
        RoleRequestDto dto = new RoleRequestDto();
        dto.setName("EDITOR_UPDATED");
        dto.setDescription("Updated editor role");
        Set<UUID> permissionIds = Set.of(permId1, permId2);
        dto.setPermissionIds(permissionIds);
        
        PermissionEntity perm1 = new PermissionEntity();
        perm1.setId(permId1);
        
        PermissionEntity perm2 = new PermissionEntity();
        perm2.setId(permId2);
        
        // Giả lập công cụ ánh xạ thực thể để trả về các quyền thử nghiệm của chúng ta
        when(entityMappingUtil.mapPermissionIdToEntity(permId1)).thenReturn(perm1);
        when(entityMappingUtil.mapPermissionIdToEntity(permId2)).thenReturn(perm2);
        
        // Khi - Thực hiện hoạt động cập nhật
        roleMapper.updateEntityFromDto(dto, role);
        roleMapper.updateRolePermissions(dto, role);
        
        // Thì - Xác minh thực thể đã được cập nhật chính xác
        assertEquals("EDITOR_UPDATED", role.getName());
        assertEquals("Updated editor role", role.getDescription());
        assertEquals(2, role.getRolePermissions().size());
        
        // Xác minh rằng cả hai quyền đã được liên kết chính xác với vai trò
        boolean foundPerm1 = false;
        boolean foundPerm2 = false;
        
        for (RolePermissionEntity rp : role.getRolePermissions()) {
            if (rp.getId().getPermissionId().equals(permId1)) {
                foundPerm1 = true;
                assertEquals(role, rp.getRole());
                assertEquals(perm1, rp.getPermission());
            }
            if (rp.getId().getPermissionId().equals(permId2)) {
                foundPerm2 = true;
                assertEquals(role, rp.getRole());
                assertEquals(perm2, rp.getPermission());
            }
        }
        
        assertTrue(foundPerm1, "Quyền 1 phải nằm trong các quyền của vai trò");
        assertTrue(foundPerm2, "Quyền 2 phải nằm trong các quyền của vai trò");
    }
}
