package org.socius.sociuswebbackend.mappers;

import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;

import org.socius.sociuswebbackend.util.ApplicationContextHelper;

/**
 * Base mapper interface providing common functionality for entity mapping
 */
public abstract class BaseEntityMapper {

    @Autowired
    protected EntityMappingUtil entityMappingUtil;

    /**
     * Get the entity mapping utility
     */
    protected EntityMappingUtil getEntityMappingUtil() {
        return entityMappingUtil != null ? entityMappingUtil
                : ApplicationContextHelper.getBean(EntityMappingUtil.class);
    }

    /**
     * Common method for setting audit fields
     */
    protected void setAuditFields(Object target) {
        // Common audit logic if needed
    }
}
