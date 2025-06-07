package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.PositionMapper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.socius.sociuswebbackend.services.PositionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionServiceImpl.class);
    final private PositionRepository positionRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
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
    @Transactional
    public PositionResponseDto create(PositionRequestDto requestDto) {
        try {
            if (positionRepository.existsByName(requestDto.getName())) {
                throw new IllegalArgumentException("Vị trí đã tồn tại");
            }

            PositionEntity position = positionMapper.requestDtoToEntity(requestDto);
            position = positionRepository.save(position);
            return positionMapper.entityToDto(position);
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi validation khi tạo vị trí: {}", e.getMessage());
            throw e; // Ném lại exception gốc
        } catch (DataIntegrityViolationException e) {
            logger.error("Lỗi ràng buộc dữ liệu khi tạo vị trí: {}", e.getMessage());
            throw new IllegalArgumentException("Không thể tạo vị trí vì ràng buộc dữ liệu");
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi tạo vị trí: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo vị trí: " + e.getMessage());
        }
    }

    @Override
    @Transactional
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

        long count = employmentDetailRepository.countByPositionId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa vị trí vì vẫn còn " + count + " nhân viên thuộc vị trí này");
        }

        positionRepository.deleteById(id);
        logger.info("Đã xóa vị trí với ID: {}", id);
    }

    @Override
    public Map<String, Object> getPositionWithMembers(UUID positionId, Pageable pageable) {
        PositionEntity position = positionRepository.findPositionWithMembers(positionId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí với ID: " + positionId));

        return positionMapper.entityToDtoWithMembers(position, pageable);
    }
}