package org.socius.sociuswebbackend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    
    /**
     * Tìm tài khoản theo người dùng
     * 
     * @param user Đối tượng người dùng cần tìm tài khoản
     * @return Optional chứa tài khoản nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<AccountEntity> findByUser(UserEntity user);

    Optional<AccountEntity> findByUser_Id(UUID userId);
}
