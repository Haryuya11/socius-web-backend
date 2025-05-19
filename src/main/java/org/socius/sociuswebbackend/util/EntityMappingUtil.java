package org.socius.sociuswebbackend.util;

import org.springframework.stereotype.Component;
import org.socius.sociuswebbackend.model.entities.*;

import java.util.UUID;

/**
 * Centralized utility class for entity mapping operations to avoid ambiguity in mappers
 */
@Component
public class EntityMappingUtil {

    /**
     * Generic method to map an ID to an entity
     */
    public <T extends BaseEntity> T mapIdToEntity(UUID id, Class<T> entityClass) {
        if (id == null) {
            return null;
        }
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            entity.setId(id);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo phiên bản entity: " + entityClass.getSimpleName(), e);
        }
    }

    public UserEntity mapUserIdToEntity(UUID id) {
        return mapIdToEntity(id, UserEntity.class);
    }

    public PositionEntity mapPositionIdToEntity(UUID id) {
        return mapIdToEntity(id, PositionEntity.class);
    }

    public DepartmentEntity mapDepartmentIdToEntity(UUID id) {
        return mapIdToEntity(id, DepartmentEntity.class);
    }

    public TeamEntity mapTeamIdToEntity(UUID id) {
        return mapIdToEntity(id, TeamEntity.class);
    }

    public RoleEntity mapRoleIdToEntity(UUID id) {
        return mapIdToEntity(id, RoleEntity.class);
    }

    public PeriodEntity mapPeriodIdToEntity(UUID id) {
        return mapIdToEntity(id, PeriodEntity.class);
    }

    public PermissionEntity mapPermissionIdToEntity(UUID id) {
        return mapIdToEntity(id, PermissionEntity.class);
    }

    public ConversationEntity mapConversationIdToEntity(UUID id) {
        return mapIdToEntity(id, ConversationEntity.class);
    }

    public MessageEntity mapMessageIdToEntity(UUID id) {
        return mapIdToEntity(id, MessageEntity.class);
    }


}
