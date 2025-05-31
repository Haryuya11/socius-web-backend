package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.SalaryHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.socius.sociuswebbackend.model.entities.UserEntity;

import java.util.UUID;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistoryEntity, UUID> {
    Page<SalaryHistoryEntity> findByUser(UserEntity user, Pageable pageable);
}
