package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.socius.sociuswebbackend.model.entities.UserEntity;

import java.util.UUID;

public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistoryEntity, UUID> {
    Page<EmploymentHistoryEntity> findByUser(UserEntity user, Pageable pageable);

}
