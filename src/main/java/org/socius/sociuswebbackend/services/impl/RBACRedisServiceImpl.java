package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RBACRedisServiceImpl implements RBACRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RBACRedisServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigService configService;

    @Override
    public void saveCacheUserPermissions(String sessionId, UserPermissionsDto permissionsDto, int expiryTimeMinutes) {
        try {
            String key = RedisKeyBuilder.rbacKey(sessionId);

            // Lưu thông tin quyền của người dùng vào Redis
            redisTemplate.opsForValue().set(key, permissionsDto, expiryTimeMinutes, TimeUnit.MINUTES);

            // Lưu mapping từ roleId sang sessionId để dễ xóa khi role thay đổi
            if (permissionsDto.getRoleId() != null) {
                String roleKey = RedisKeyBuilder.roleUsersKey(permissionsDto.getRoleId());
                redisTemplate.opsForSet().add(roleKey, sessionId);
            }

            logger.info("Đã lưu cache quyền hạn cho người dùng: {}, với role: {}", permissionsDto.getUserId(), permissionsDto.getRoleName());
        } catch (Exception e) {
            logger.error("Lỗi khi lưu quyền hạn vào Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public UserPermissionsDto getUserPermissions(String sessionId) {
        try {
            String key = RedisKeyBuilder.rbacKey(sessionId);

            // Lấy thông tin quyền của người dùng từ Redis
            return (UserPermissionsDto) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy quyền hạn từ Redis: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean hasPermission(String sessionId, String permission) {
        try {
            UserPermissionsDto permissionsDto = getUserPermissions(sessionId);
            if (permissionsDto != null && permissionsDto.getPermissions() != null) {
                return permissionsDto.getPermissions().contains(permission);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra quyền hạn từ Redis: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean hasRole(String sessionId, String roleName) {
        try {
            UserPermissionsDto permissionsDto = getUserPermissions(sessionId);
            if (permissionsDto != null && permissionsDto.getRoleName() != null) {
                return permissionsDto.getRoleName().equals(roleName);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra vai trò từ Redis: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void deleteUserPermissions(String sessionId) {
        try {
            String key = RedisKeyBuilder.rbacKey(sessionId);
            Object value = redisTemplate.opsForValue().get(key);

            UserPermissionsDto permissionsDto = null;
            if (value instanceof UserPermissionsDto) {
                permissionsDto = (UserPermissionsDto) value;
            } else if (value instanceof Set) {
                // Chuyển đổi từ Set sang DTO thông qua ObjectMapper
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                permissionsDto = mapper.convertValue(value, UserPermissionsDto.class);
            }

            UUID userId = permissionsDto != null ? permissionsDto.getUserId() : null;
            String roleName = permissionsDto != null ? permissionsDto.getRoleName() : null;
            UUID roleId = permissionsDto != null ? permissionsDto.getRoleId() : null;


            if (roleId != null) {
                String roleKey = RedisKeyBuilder.roleUsersKey(roleId);
                redisTemplate.opsForSet().remove(roleKey, sessionId);
            }

            // Xóa thông tin quyền của người dùng khỏi Redis
            redisTemplate.delete(key);

            if (userId != null && roleName != null) {
                logger.info("Đã xóa cache quyền hạn cho người dùng: {}, với role: {}", userId, roleName);
            } else {
                logger.info("Đã xóa cache quyền hạn cho phiên: {}", sessionId);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xóa quyền hạn từ Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public long deleteByRoleId(UUID roleId) {
        try {
            String roleKey = RedisKeyBuilder.roleUsersKey(roleId);

            // Lấy danh sách sessionId liên quan đến roleId
            Set<Object> sessionIds = redisTemplate.opsForSet().members(roleKey);


            // Xóa tất cả sessionId liên quan đến roleId
            long count = 0;
            if (sessionIds != null && !sessionIds.isEmpty()) {
                // Xóa tất cả sessionId liên quan đến roleId
                for (Object sessionId : sessionIds) {
                    String key = RedisKeyBuilder.rbacKey((String) sessionId);
                    boolean deleted = redisTemplate.delete(key);
                    if (deleted) {
                        count++;
                    }
                }
            }
            // Xóa roleKey sau khi xóa tất cả sessionId
            redisTemplate.delete(roleKey);

            logger.info("Đã xóa {} quyền hạn liên quan đến vai trò: {}", count, roleId);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi xóa quyền hạn theo role từ Redis: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean extendExpiration(String sessionId, int expiryTimeMinutes) {
        try {
            String key = RedisKeyBuilder.rbacKey(sessionId);

            // Kiểm tra xem key có tồn tại
            boolean exists = redisTemplate.hasKey(key);
            if (!exists) {
                logger.warn("Key không tồn tại trong Redis: {}", key);
                return false;
            }

            // Lấy giá trị hiện tại của key
            UserPermissionsDto permissionsDto = (UserPermissionsDto) redisTemplate.opsForValue().get(key);
            if (permissionsDto == null) {
                logger.warn("Giá trị của key không tồn tại trong Redis: {}", key);
                return false;
            }

            // Gia hạn thời gian hết hạn
            redisTemplate.expire(key, expiryTimeMinutes, TimeUnit.MINUTES);
            logger.info("Đã gia hạn thời gian của phiên: {} thêm: {} phút", key, expiryTimeMinutes);
            return true;
        } catch (Exception e) {
            logger.error("Lỗi khi gia hạn thời gian hết hạn của phiên: {}", e.getMessage(), e);
            return false;
        }
    }
}
