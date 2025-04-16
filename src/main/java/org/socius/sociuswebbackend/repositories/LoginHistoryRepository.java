package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.LoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistoryEntity, UUID> {
    
    /**
     * Tìm lịch sử đăng nhập theo ID người dùng, sắp xếp theo thời gian đăng nhập giảm dần
     * 
     * @param userId ID của người dùng cần tìm lịch sử đăng nhập
     * @return Danh sách lịch sử đăng nhập của người dùng, sắp xếp theo thời gian đăng nhập mới nhất đầu tiên
     */
    List<LoginHistoryEntity> findByUserIdOrderByLoginTimeDesc(UUID userId);
}
