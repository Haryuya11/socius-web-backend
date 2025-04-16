package org.socius.sociuswebbackend.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementServiceImpl.class);
    private static final String SESSION_USER_KEY = "USER_ID";

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<OnlineUserDto> getOnlineUsers() {
        List<OnlineUserDto> onlineUsers = new ArrayList<>();
        
        try {
            // Lấy tất cả các session keys từ Redis
            Set<String> sessionIds = (Set<String>) sessionRepository.findByPrincipalName("*");
            
            for (String sessionId : sessionIds) {
                Session session = sessionRepository.findById(sessionId);
                
                if (session != null) {
                    UUID userId = session.getAttribute(SESSION_USER_KEY);
                    
                    if (userId != null) {
                        Optional<UserEntity> userOpt = userRepository.findById(userId);
                        
                        if (userOpt.isPresent()) {
                            UserEntity user = userOpt.get();
                            
                            OnlineUserDto onlineUser = OnlineUserDto.builder()
                                    .userId(userId)
                                    .fullName(user.getFirstName() + " " + user.getLastName())
                                    .imageUrl(user.getImageUrl())
                                    .build();
                            
                            onlineUsers.add(onlineUser);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng đang online", e);
        }
        
        return onlineUsers;
    }

    @Override
    public boolean isUserActive(String userId) {
        List<OnlineUserDto> activeUsers = getOnlineUsers();
        return activeUsers.stream()
                .anyMatch(user -> user.getUserId().toString().equals(userId));
    }
}