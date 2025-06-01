package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic mapper interface defining standard conversion methods
 *
 * @param <E> Entity type
 * @param <R> Response DTO type
 * @param <Q> Request DTO type
 */
public interface GenericMapper<E, R, Q> {

    /**
     * Convert entity to response DTO
     */
    R entityToDto(E entity);

    /**
     * Convert request DTO to new entity
     */
    E requestDtoToEntity(Q dto);

    /**
     * Update existing entity from request DTO
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(Q dto, @MappingTarget E entity);

    /**
     * Convert list of entities to list of response DTOs
     */
    default List<R> entitiesToDtos(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert set of entities to set of response DTOs
     */
    default Set<R> entitiesToDtos(Set<E> entities) {
        if (entities == null) {
            return Set.of();
        }
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toSet());
    }
}
