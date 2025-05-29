package org.socius.sociuswebbackend.repositories;

import java.util.UUID;

import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    /**
     * Kiểm tra xem nhóm có tồn tại hay không
     * 
     * @param name Tên nhóm cần kiểm tra
     * @return true nếu nhóm tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.employmentDetailEntities e WHERE t.id = :teamId")
    Page<TeamEntity> findTeamWithMembers(@Param("teamId") UUID teamId, Pageable pageable);
    
}
