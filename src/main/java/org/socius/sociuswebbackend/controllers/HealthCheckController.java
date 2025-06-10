package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {
    final private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/websocket")
    public ResponseEntity<Map<String, Object>> checkWebSocketHealth() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra Redis connection
            redisTemplate.opsForValue().get("health:check");
            response.put("redis", "UP");

            // Kiểm tra WebSocket connection
            String pattern = RedisKeyBuilder.userOnlinePattern();
            Set<String> onlineUsers = redisTemplate.keys(pattern);
            response.put("onlineUsers", onlineUsers.size());

            response.put("status", "UP");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}
