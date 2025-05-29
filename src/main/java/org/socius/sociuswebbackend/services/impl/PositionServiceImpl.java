package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.PositionMapper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.PositionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionServiceImpl.class);
    final private PositionRepository positionRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private UserRepository userRepository;
    final private EmploymentHistoryRepository employmentHistoryRepository;

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

        long count = employmentDetailRepository.countByPositionId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa vị trí vì vẫn còn " + count + " nhân viên đang giữ vị trí này");

        }
        positionRepository.deleteById(id);
        logger.info("Đã xóa vị trí với ID: {}", id);
    }

    @Override
    public PositionResponseDto addEmployee(UUID positionId, UUID employeeId) {
        PositionEntity position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm của nhân viên"));

        employmentDetail.setPosition(position);
        employmentDetailRepository.save(employmentDetail);

        logger.info("Đã thêm nhân viên {} vào vị trí {}", employee.getEmail(), position.getName());
        return positionMapper.entityToDto(position);
    }

    @Override
    public PositionResponseDto removeEmployee(UUID positionId, UUID employeeId) {
        PositionEntity position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm của nhân viên"));

        if (employmentDetail.getPosition() == null || !employmentDetail.getPosition().getId().equals(positionId)) {
            throw new RuntimeException("Nhân viên không thuộc vị trí này");
        }

        EmploymentHistoryEntity employmentHistory = EmploymentHistoryEntity.builder()
                .user(employee)
                .position(employmentDetail.getPosition())
                .department(employmentDetail.getDepartment())
                .team(employmentDetail.getTeam())
                .role(employmentDetail.getRole())
                .startDate(employmentDetail.getStartDate())
                .endDate(LocalDate.now())
                .salary(employmentDetail.getSalary())
                .description("Đã rời khỏi vị trí " + position.getName())
                .build();

        employmentHistoryRepository.save(employmentHistory);

        employmentDetail.setPosition(null);
        employmentDetailRepository.save(employmentDetail);

        logger.info("Đã xóa nhân viên {} khỏi vị trí {}", employee.getEmail(), position.getName());
        return positionMapper.entityToDto(position);
    }

    @Override
    public List<PositionResponseDto> addEmployees(UUID positionId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(positionId, employeeId);
        }
        return findAll();
    }

    @Override
    public List<PositionResponseDto> removeEmployees(UUID positionId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(positionId, employeeId);
        }
        return findAll();
    }
}
