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
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OnlineUserServiceImplTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigService configService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OnlineUserServiceImpl onlineUserService;

    private UserEntity adminUser;
    private UserEntity RegularUser;
    private OnlineUserStatusDto adminStatusDto;
    private OnlineUserStatusDto regularStatusDto;
//    private final String ONLINE_USERS_PREFIX = "online:users:";

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
        RegularUser = AuthTestDataUtil.createTestRegularUser();

        adminStatusDto = OnlineUserStatusDto.builder()
                .userId(adminUser.getId())
                .fullName(adminUser.getFullName())
                .imageUrl(adminUser.getImageUrl())
                .sessionId("sessionId1")
                .lastSeen(LocalDateTime.now())
                .build();

        regularStatusDto = OnlineUserStatusDto.builder()
                .userId(RegularUser.getId())
                .fullName(RegularUser.getFullName())
                .imageUrl(RegularUser.getImageUrl())
                .sessionId("sessionId2")
                .lastSeen(LocalDateTime.now())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(configService.getInt(eq("online.status.timeout.minutes"), anyInt())).thenReturn(5);
        when(configService.getInt(eq("session_timeout"), anyInt())).thenReturn(60);
    }

    @Test
    @DisplayName("Cập nhật trạng thái người dùng online thành công")
    void updateOnlineUserStatusShouldSucceed() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        onlineUserService.updateUserOnlineStatus(adminUser.getId(), "sessionId1");

        verify(valueOperations).set(eq(key), any(OnlineUserStatusDto.class));
        verify(redisTemplate).expire(eq(key), eq(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("Xử lý heartbeat thành công khi người dùng đang online")
    void handleUserHeartbeatShouldUpdateLastSeenWhenUserIsOnline() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        String sessionKey = RedisKeyBuilder.springSessionKey(adminStatusDto.getSessionId());


        when(valueOperations.get(key)).thenReturn(adminStatusDto);
        when(redisTemplate.hasKey(sessionKey)).thenReturn(true);
        when(redisTemplate.getExpire(sessionKey)).thenReturn(1800L);

        onlineUserService.handleUserHeartbeat(adminUser.getId());

        verify(valueOperations).set(eq(key), any(OnlineUserStatusDto.class));
    }

    @Test
    @DisplayName("Xử lý heartbeat không làm gì khi người dùng không online")
    void handleUserHeartbeatShouldDoNothingWhenUserIsNotOnline() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        when(valueOperations.get(key)).thenReturn(null);

        onlineUserService.handleUserHeartbeat(adminUser.getId());

        verify(valueOperations, never()).set(eq(key), any(OnlineUserStatusDto.class));
    }

    @Test
    @DisplayName("Đánh dấu người dùng offline thành công")
    void markUserOfflineShouldSucceed() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        when(valueOperations.get(key)).thenReturn(adminStatusDto);

        onlineUserService.markUserOffline(adminUser.getId(), "sessionId1");

        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("Lấy trạng thái người dùng online thành công")
    void getUserOnlineStatusShouldSucceed() {
        String pattern = RedisKeyBuilder.getKeyPattern("user:") + ":online";

        Set<String> keys = new HashSet<>(Arrays.asList(
                RedisKeyBuilder.userOnlineKey(adminUser.getId()),
                RedisKeyBuilder.userOnlineKey(RegularUser.getId())
        ));
        when(redisTemplate.keys(pattern)).thenReturn(keys);

        List<Object> onlineUsers = new ArrayList<>();
        onlineUsers.add(adminStatusDto);
        onlineUsers.add(regularStatusDto);

        when(redisTemplate.opsForValue().multiGet(any())).thenReturn(onlineUsers);
        when(configService.getInt(eq("online.status.timeout.minutes"), anyInt())).thenReturn(5);

        List<OnlineUserStatusDto> result = onlineUserService.getOnlineUsers();

        assertEquals(2, result.size(), "Kết quả không khớp với số lượng người dùng online");
        assertTrue(result.stream().anyMatch(dto -> dto.getUserId().equals(adminUser.getId())), "Không tìm thấy trạng thái người dùng admin");
        assertTrue(result.stream().anyMatch(dto -> dto.getUserId().equals(RegularUser.getId())), "Không tìm thấy trạng thái người dùng thường");
    }

    @Test
    @DisplayName("Kiểm tra người dùng đang online thành công")
    void isUserOnlineShouldSucceed() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        when(valueOperations.get(key)).thenReturn(adminStatusDto);

        boolean result = onlineUserService.isUserOnline(adminUser.getId());

        assertTrue(result, "Người dùng phải đang online");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Kiểm tra người dùng không online khi không có trong Redis")
    void isUserOnlineShouldReturnFalseWhenNotInRedis() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        when(valueOperations.get(key)).thenReturn(null);

        boolean result = onlineUserService.isUserOnline(adminUser.getId());

        assertFalse(result, "Người dùng phải không online");
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Kiểm tra người dùng không online khi lastSeen quá cũ")
    void isUserOnlineShouldReturnFalseWhenLastSeenTooOld() {
        String key = RedisKeyBuilder.userOnlineKey(adminUser.getId());
        OnlineUserStatusDto oldStatusDto = OnlineUserStatusDto.builder()
                .userId(adminUser.getId())
                .fullName(adminUser.getFullName())
                .imageUrl(adminUser.getImageUrl())
                .sessionId("admin-session-id")
                .lastSeen(LocalDateTime.now().minusMinutes(10))
                .build();

        when(valueOperations.get(key)).thenReturn(oldStatusDto);
        when(configService.getInt(eq("online.status.timeout.minutes"), anyInt())).thenReturn(5);
        boolean result = onlineUserService.isUserOnline(adminUser.getId());
        assertFalse(result, "Người dùng phải không online");
        verify(valueOperations).get(key);
    }
}
