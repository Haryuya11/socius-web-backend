package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    Page<TaskEntity> findByAssignedToId(UUID userId, Pageable pageable);
}
