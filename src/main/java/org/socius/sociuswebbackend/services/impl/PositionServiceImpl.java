package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.PositionMapper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.PositionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    @Transactional
    public PositionResponseDto create(PositionRequestDto requestDto) {
        try {
            if (positionRepository.existsByName(requestDto.getName())) {
                throw new RuntimeException("Vị trí đã tồn tại");
            }

            PositionEntity position = positionMapper.requestDtoToEntity(requestDto);
            position = positionRepository.save(position);
            return positionMapper.entityToDto(position);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể tạo vị trí vì ràng buộc dữ liệu", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo vị trí: " + e.getMessage(), e);
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
            throw new IllegalStateException("Không thể xóa vị trí vì vẫn còn " + count + " nhân viên đang giữ vị trí này");
        }

        positionRepository.deleteById(id);
        logger.info("Đã xóa vị trí với ID: {}", id);
    }

    @Override
    public void addEmployee(UUID positionId, UUID employeeId) {
        // Tìm vị trí theo ID
        positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        // Tìm nhân viên theo ID
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Kiểm tra xem nhân viên đã thuộc vị trí chưa
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc vị trí không
        if (employmentDetail.getPosition() != null &&
                employmentDetail.getPosition().getId().equals(positionId)) {
            throw new RuntimeException("Nhân viên đã thuộc vị trí này");
        }

        addEmployeeToDatabase(positionId, employeeId);
    }

    @Transactional
    public void addEmployeeToDatabase(UUID positionId, UUID employeeId) {
        // Logic thêm nhân viên vào database
        PositionEntity position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getPosition() != null &&
                !employmentDetail.getPosition().getId().equals(positionId)) {
            throw new RuntimeException("Nhân viên đã thuộc vị trí khác");
        }

        employmentDetail.setPosition(position);
        employmentDetailRepository.save(employmentDetail);

        logger.info("Đã thêm nhân viên {} vào vị trí {}", employee.getEmail(), position.getName());
        positionMapper.entityToDto(position);
    }

    @Override
    public void removeEmployee(UUID positionId, UUID employeeId) {
        // Kiểm tra điều kiện trước khi thực hiện
        positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc vị trí không
        if (employmentDetail.getPosition() == null || !employmentDetail.getPosition().getId().equals(positionId)) {
            throw new RuntimeException("Nhân viên không thuộc vị trí này");
        }

        // Thực hiện cập nhật database
        removeEmployeeFromDatabase(positionId, employeeId);
    }

    @Transactional
    public void removeEmployeeFromDatabase(UUID positionId, UUID employeeId) {
        try {
            // Tìm vị trí theo ID
            PositionEntity position = positionRepository.findById(positionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

            // Tìm nhân viên theo ID
            UserEntity employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

            // Tìm chi tiết việc làm của nhân viên
            EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

            // Lưu lịch sử làm việc
            EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
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

            employmentHistoryRepository.save(history);

            // Xóa nhân viên khỏi vị trí
            employmentDetail.setPosition(null);
            employmentDetailRepository.save(employmentDetail);

            logger.info("Đã xóa nhân viên {} khỏi vị trí {}", employee.getEmail(), position.getName());
            positionMapper.entityToDto(position);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa nhân viên {} khỏi vị trí {}: {}",
                    employeeId, positionId, e.getMessage());
            throw new RuntimeException("Không thể xóa nhân viên khỏi vị trí: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<PositionResponseDto> addEmployees(UUID positionId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(positionId, employeeId);
        }
        return findAll();
    }

    @Override
    @Transactional
    public List<PositionResponseDto> removeEmployees(UUID positionId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(positionId, employeeId);
        }
        return findAll();
    }

    @Override
    public Map<String, Object> getPositionWithMembers(UUID positionId, Pageable pageable) {
        PositionEntity position = positionRepository.findPositionWithMembers(positionId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Position not found with ID: " + positionId));

        return positionMapper.entityToDtoWithMembers(position, pageable);
    }
}