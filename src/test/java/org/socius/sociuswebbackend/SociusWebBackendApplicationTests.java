package org.socius.sociuswebbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@SpringBootTest
class SociusWebBackendApplicationTests {

    @MockBean
    private RedisIndexedSessionRepository redisIndexedSessionRepository;
    
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
    }
}