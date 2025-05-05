package org.socius.sociuswebbackend.services.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.socius.sociuswebbackend.events.RBACEvent;
import org.socius.sociuswebbackend.mappers.RoleMapper;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.repositories.RoleRepository;
import org.socius.sociuswebbackend.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<RoleResponseDto> findAll() {
        List<RoleResponseDto> roles = roleRepository.findAll().stream()
                .map(roleMapper::entityToDto)
                .toList();
        return roles;
    }

    @Override
    public RoleResponseDto findById(UUID id) {
        RoleResponseDto role = roleRepository.findById(id)
                .map(roleMapper::entityToDto)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));
        return role;
    }

    @Override
    public RoleResponseDto create(RoleRequestDto requestDto) {
        if (roleRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Vai trò đã tồn tại");
        } else {
            RoleEntity role = roleMapper.requestDtoToEntity(requestDto);
            role = roleRepository.save(role);
            return roleMapper.entityToDto(role);
        }
    }

    @Override
    @Transactional
    public RoleResponseDto update(UUID id, RoleRequestDto requestDto) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));

        if (!role.getName().equals(requestDto.getName()) &&
                roleRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Vai trò với tên này đã tồn tại");
        }

        role.setName(requestDto.getName());
        role.setDescription(requestDto.getDescription());

        // Cập nhật các quyền
        if (requestDto.getPermissionIds() != null && !requestDto.getPermissionIds().isEmpty()) {
            roleMapper.updateRolePermissions(requestDto, role);
        }
        role = roleRepository.save(role);
        // Phát sự kiện cập nhật vai trò
        eventPublisher.publishEvent(new RBACEvent(this, id, RBACEvent.EventType.ROLE_UPDATED));

        return roleMapper.entityToDto(role);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy vai trò với ID: " + id);
        }

        // Phát sự kiện xóa vai trò
        eventPublisher.publishEvent(new RBACEvent(this, id, RBACEvent.EventType.ROLE_DELETED));

        roleRepository.deleteById(id);
    }

}
