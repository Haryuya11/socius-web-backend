package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistoryEntity, UUID> {
}
