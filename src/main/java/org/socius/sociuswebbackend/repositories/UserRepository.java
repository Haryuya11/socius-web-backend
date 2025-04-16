package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
    /**
     * Tìm người dùng theo địa chỉ email
     * 
     * @param email Địa chỉ email của người dùng cần tìm
     * @return Optional chứa người dùng nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<UserEntity> findByEmail(String email);
}
