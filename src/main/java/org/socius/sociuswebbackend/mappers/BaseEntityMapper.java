package org.socius.sociuswebbackend.mappers;

import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;

/**
 * Base mapper interface providing common functionality for entity mapping
 */
public interface BaseEntityMapper {
    /**
     * Get the entity mapping utility
     */
    default EntityMappingUtil getEntityMappingUtil() {
        return ApplicationContextHelper.getBean(EntityMappingUtil.class);
    }
}
