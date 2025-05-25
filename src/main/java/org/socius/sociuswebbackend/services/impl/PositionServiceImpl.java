package org.socius.sociuswebbackend.services.impl;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.PositionMapper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.socius.sociuswebbackend.services.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    final private PositionRepository positionRepository;

    final private PositionMapper positionMapper;

    @Override
    public List<PositionResponseDto> findAll() {
        List<PositionEntity> positions = positionRepository.findAll();
        return positions.stream()
                .map(positionMapper::entityToDto)
                .toList();
    }

    @Override
    public PositionResponseDto findById(UUID id) {
        PositionEntity position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + id));
        return positionMapper.entityToDto(position);
    }

    @Override
    public PositionResponseDto create(PositionRequestDto requestDto) {
        if (positionRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Vị trí đã tồn tại");
        } else {
            PositionEntity position = positionMapper.requestDtoToEntity(requestDto);
            position = positionRepository.save(position);
            return positionMapper.entityToDto(position);
        }
    }

    @Override
    public PositionResponseDto update(UUID id, PositionRequestDto requestDto) {
        PositionEntity position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + id));

        if (!position.getName().equals(requestDto.getName()) &&
                positionRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Vị trí với tên này đã tồn tại");
        }

        positionMapper.updateEntityFromDto(requestDto, position);
        position = positionRepository.save(position);
        return positionMapper.entityToDto(position);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!positionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy vị trí với ID: " + id);
        }
        positionRepository.deleteById(id);
    }

}
