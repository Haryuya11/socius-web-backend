package org.socius.sociuswebbackend.repositories;

import java.util.List;
import java.util.UUID;

import io.lettuce.core.dynamic.annotation.Param;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    /**
     * Kiểm tra xem nhóm có tồn tại hay không
     *
     * @param name Tên nhóm cần kiểm tra
     * @return true nếu nhóm tồn tại, false nếu không tồn tại
     */
    boolean existsByName(String name);

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.employmentDetail e WHERE t.id = :teamId")
    Page<TeamEntity> findTeamWithMembers(@Param("teamId") UUID teamId, Pageable pageable);

    /**
     * Lấy tất cả các nhóm đang hoạt động
     *
     * @return Danh sách các nhóm đang hoạt động
     */
    @Query("SELECT t FROM TeamEntity t WHERE t.status = 'active'")
    List<TeamEntity> findAllActiveTeams();
}
