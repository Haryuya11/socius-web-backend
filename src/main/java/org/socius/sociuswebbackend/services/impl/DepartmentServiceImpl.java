package org.socius.sociuswebbackend.services.impl;

import java.util.List;
import java.util.UUID;

import org.socius.sociuswebbackend.mappers.DepartmentMapper;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.socius.sociuswebbackend.repositories.DepartmentRepository;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentResponseDto> findAll() {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        return departments.stream()
                .map(departmentMapper::entityToDto)
                .toList();
    }

    @Override
    public DepartmentResponseDto findById(UUID id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        return departmentMapper.entityToDto(department);
    }

    @Override
    public DepartmentResponseDto create(DepartmentRequestDto requestDto) {
        if (departmentRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Phòng ban đã tồn tại");
        } else {
            DepartmentEntity department = departmentMapper.requestDtoToEntity(requestDto);
            department = departmentRepository.save(department);
            return departmentMapper.entityToDto(department);
        }
    }

    @Override
    public DepartmentResponseDto update(UUID id, DepartmentRequestDto requestDto) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        if (!department.getName().equals(requestDto.getName()) &&
                departmentRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Phòng ban với tên này đã tồn tại");
        }

        departmentMapper.updateEntityFromDto(requestDto, department);
        department = departmentRepository.save(department);
        return departmentMapper.entityToDto(department);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy phòng ban với ID: " + id);
        }
        departmentRepository.deleteById(id);
    }

}
