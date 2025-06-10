package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    Page<TaskEntity> findByAssignedToId(UUID userId, Pageable pageable);

    @Query("SELECT t FROM TaskEntity t WHERE t.assignedTo.id IN :userIds")
    Page<TaskEntity> findByManyAssignedToId(List<UUID> userIds, Pageable pageable);

    @Query("SELECT t FROM TaskEntity t WHERE t.status NOT IN :excludedStatuses AND t.deadline < :currentDate")
    List<TaskEntity> findOverdueTasksNotInStatus(LocalDate currentDate, List<TaskStatus> excludedStatuses);
}
