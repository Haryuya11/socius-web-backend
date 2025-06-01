package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.entities.BaseEntity;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void mapForCreate(Object source, @MappingTarget BaseEntity target);

}
