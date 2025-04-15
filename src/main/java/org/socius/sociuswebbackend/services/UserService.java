package org.socius.sociuswebbackend.services;

import java.util.UUID;

import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

public interface UserService {
    /**
     * Tìm người dùng theo ID
     * 
     * @param userId ID của người dùng cần tìm
     * @return Thông tin người dùng hoặc null nếu không tìm thấy
     */
    UserResponseDto findById(UUID userId);
}