package org.socius.sociuswebbackend.services;

import java.util.UUID;

public interface SessionValidationService {
    
    /**
     * Kiểm tra session có hợp lệ không
     * 
     * @param sessionId ID của session cần kiểm tra
     * @return true nếu session hợp lệ, false nếu không
     */
    boolean isSessionValid(String sessionId);
    
    /**
     * Kiểm tra user có session hợp lệ không
     * 
     * @param userId ID của user cần kiểm tra
     * @return true nếu user có session hợp lệ, false nếu không
     */
    boolean hasValidSession(UUID userId);
    
    /**
     * Lấy sessionId của user (nếu có session hợp lệ)
     * 
     * @param userId ID của user
     * @return sessionId hoặc null nếu không có session hợp lệ
     */
    String getUserSessionId(UUID userId);
}