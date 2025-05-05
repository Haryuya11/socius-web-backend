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
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RBACRedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ConfigService configService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RBACRedisServiceImpl rbacRedisService;

    private final String sessionId = "test-session-id";
    private final String rbacPrefix = "rbac:";
    private final String roleUsersPrefix = "role:users:";
    private UserPermissionsDto adminPermissionsDto;
    private final UUID adminRoleId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        adminPermissionsDto = AuthTestDataUtil.createAdminPermissionsDto();

        when(configService.getString(eq("rbac.key.prefix"), anyString())).thenReturn(rbacPrefix);
        when(configService.getString(eq("rbac.role.users.prefix"), anyString())).thenReturn(roleUsersPrefix);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Lưu bộ nhớ đệm quyền người dùng phải lưu trữ quyền trong Redis")
    void saveCacheUserPermissionsShouldStorePermissionsInRedis() {
        int expiryTimeMinutes = 30;
        String key = rbacPrefix + sessionId;
        String roleKey = roleUsersPrefix + adminRoleId;

        rbacRedisService.saveCacheUserPermissions(sessionId, adminPermissionsDto, expiryTimeMinutes);

        verify(valueOperations).set(eq(key), eq(adminPermissionsDto), eq((long) expiryTimeMinutes), eq(TimeUnit.MINUTES));
        verify(setOperations).add(eq(roleKey), eq(sessionId));
    }

    @Test
    @DisplayName("Get user permissions should retrieve permissions from Redis")
    void getUserPermissionsShouldRetrievePermissionsFromRedis() {
        String key = rbacPrefix + sessionId;

        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);

        UserPermissionsDto result = rbacRedisService.getUserPermissions(sessionId);

        assertNotNull(result, "Kết quả không được null");
        assertEquals(adminPermissionsDto, result, "Kết quả không khớp với quyền đã lưu trữ");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Has permission should return true when user has the permission")
    void hasPermissionShouldReturnTrueWhenUserHasPermission() {
        String key = rbacPrefix + sessionId;
        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);
        boolean result = rbacRedisService.hasPermission(sessionId, "USER_CREATE");

        assertTrue(result, "Người dùng phải có quyền");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Has permission should return false when user doesn't have the permission")
    void hasPermissionShouldReturnFalseWhenUserDoesNotHavePermission() {
        String key = rbacPrefix + sessionId;
        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);

        boolean result = rbacRedisService.hasPermission(sessionId, "NOT_EXISTING_PERMISSION");

        assertFalse(result, "Người dùng không có quyền");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Has role should return true when user has the role")
    void hasRoleShouldReturnTrueWhenUserHasRole() {
        String key = rbacPrefix + sessionId;
        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);

        boolean result = rbacRedisService.hasRole(sessionId, "ADMIN");

        assertTrue(result, "Người dùng phải có vai trò");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Has role should return false when user doesn't have the role")
    void hasRoleShouldReturnFalseWhenUserDoesNotHaveRole() {
        String key = rbacPrefix + sessionId;
        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);

        boolean result = rbacRedisService.hasRole(sessionId, "USER");

        assertFalse(result, "Người dùng không có vai trò");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Delete user permissions should remove permissions from Redis")
    void deleteUserPermissionsShouldRemovePermissionsFromRedis() {
        String key = rbacPrefix + sessionId;
        String roleKey = roleUsersPrefix + adminRoleId;

        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);
        when(redisTemplate.delete(key)).thenReturn(true);

        rbacRedisService.deleteUserPermissions(sessionId);

        verify(setOperations).remove(eq(roleKey), eq(sessionId));
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("Delete by role ID should remove all user permissions for that role")
    void deleteByRoleIdShouldRemoveAllUserPermissionsForThatRole() {
        String key = rbacPrefix + sessionId;
        String roleKey = roleUsersPrefix + adminRoleId;
        Set<Object> sessionIds = new HashSet<>();
        sessionIds.add(sessionId);
        sessionIds.add("another-session-id");

        when(setOperations.members(roleKey)).thenReturn(sessionIds);

        when(valueOperations.get(rbacPrefix + sessionId)).thenReturn(adminPermissionsDto);
        when(valueOperations.get(rbacPrefix + "another-session-id")).thenReturn(adminPermissionsDto);

        when(redisTemplate.delete(anyString())).thenReturn(true);

        long count = rbacRedisService.deleteByRoleId(adminRoleId);

        assertEquals(2, count, "Số lượng phiên không khớp");
        verify(redisTemplate).delete(roleKey);

        verify(redisTemplate).delete(rbacPrefix + sessionId);
        verify(redisTemplate).delete(rbacPrefix + "another-session-id");

        verify(redisTemplate).delete(roleKey);
    }

    @Test
    @DisplayName("Delete by role ID should handle empty set gracefully")
    void deleteByRoleIdShouldHandleEmptySetGracefully() {
        String roleKey = roleUsersPrefix + adminRoleId;
        when(setOperations.members(roleKey)).thenReturn(new HashSet<>());

        long count = rbacRedisService.deleteByRoleId(adminRoleId);

        assertEquals(0, count, "Số lượng phiên không khớp");
        verify(setOperations).members(roleKey);
        verify(redisTemplate).delete(roleKey);
    }

    @Test
    @DisplayName("Delete by role ID should handle exceptions gracefully")
    void deleteByRoleIdShouldHandleExceptionsGracefully(){
        String roleKey = roleUsersPrefix + adminRoleId;
        when(setOperations.members(roleKey)).thenThrow(new RuntimeException("Test exception"));

        long count = rbacRedisService.deleteByRoleId(adminRoleId);

        assertEquals(0, count, "Số lượng phiên không khớp");
        verify(setOperations).members(roleKey);
    }

    @Test
    @DisplayName("Extend expiration should update TTL for cache entry")
    void extendExpirationShouldUpdateTTLForCacheEntry() {
        String key = rbacPrefix + sessionId;
        int expiryTimeMinutes = 30;

        when(redisTemplate.hasKey(key)).thenReturn(true);
        when(valueOperations.get(key)).thenReturn(adminPermissionsDto);
        when(redisTemplate.expire(key, expiryTimeMinutes, TimeUnit.MINUTES)).thenReturn(true);

        boolean result = rbacRedisService.extendExpiration(sessionId, expiryTimeMinutes);

        assertTrue(result, "Thời gian sống phải được gia hạn");
        verify(redisTemplate).hasKey(key);
        verify(redisTemplate).expire(key, expiryTimeMinutes, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Extend expiration should return false when key doesn't exist")
    void extendExpirationShouldReturnFalseWhenKeyDoesNotExist() {
        String key = rbacPrefix + sessionId;
        int expiryTimeMinutes = 30;

        when(redisTemplate.hasKey(key)).thenReturn(false);

        boolean result = rbacRedisService.extendExpiration(sessionId, expiryTimeMinutes);

        assertFalse(result, "Thời gian sống phải không được gia hạn");
        verify(redisTemplate).hasKey(key);
        verify(redisTemplate, never()).expire(anyString(), anyInt(), any(TimeUnit.class));
    }
}
