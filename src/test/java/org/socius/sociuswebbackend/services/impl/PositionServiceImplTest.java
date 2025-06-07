package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.PositionMapper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PositionServiceImplTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionServiceImpl positionService;

    private PositionEntity testPosition;
    private PositionRequestDto testPositionRequestDto;
    private PositionResponseDto testPositionResponseDto;

    @BeforeEach
    void setUp() {
        // Test Position Entity
        testPosition = PositionEntity.builder()
                .id(UUID.randomUUID())
                .name("Software Engineer")
                .description("Software Engineer Position")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Test Position Request DTO
        testPositionRequestDto = PositionRequestDto.builder()
                .name("Software Engineer")
                .description("Software Engineer Position")
                .build();

        // Test Position Response DTO
        testPositionResponseDto = PositionResponseDto.builder()
                .id(testPosition.getId())
                .name("Software Engineer")
                .description("Software Engineer Position")
                .createdAt(testPosition.getCreatedAt())
                .updatedAt(testPosition.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Tìm tất cả positions thành công")
    void findAllShouldReturnAllPositions() {
        // Given
        List<PositionEntity> positions = Collections.singletonList(testPosition);
        when(positionRepository.findAll()).thenReturn(positions);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        List<PositionResponseDto> result = positionService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPositionResponseDto.getName(), result.getFirst().getName());
        verify(positionRepository).findAll();
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Tìm position theo ID thành công")
    void findByIdShouldReturnPosition() {
        // Given
        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        PositionResponseDto result = positionService.findById(testPosition.getId());

        // Then
        assertNotNull(result);
        assertEquals(testPositionResponseDto.getName(), result.getName());
        verify(positionRepository).findById(testPosition.getId());
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Tìm position theo ID không tồn tại nên throw exception")
    void findByIdShouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(positionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.findById(nonExistentId)
        );

        assertEquals("Không tìm thấy vị trí với ID: " + nonExistentId, exception.getMessage());
        verify(positionRepository).findById(nonExistentId);
        verify(positionMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Tạo position thành công")
    void createShouldReturnCreatedPosition() {
        // Given
        when(positionRepository.existsByName(testPositionRequestDto.getName())).thenReturn(false);
        when(positionMapper.requestDtoToEntity(testPositionRequestDto)).thenReturn(testPosition);
        when(positionRepository.save(testPosition)).thenReturn(testPosition);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        PositionResponseDto result = positionService.create(testPositionRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(testPositionResponseDto.getName(), result.getName());
        verify(positionRepository).existsByName(testPositionRequestDto.getName());
        verify(positionRepository).save(testPosition);
        verify(positionMapper).requestDtoToEntity(testPositionRequestDto);
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Tạo position với tên đã tồn tại nên throw exception")
    void createShouldThrowExceptionWhenNameExists() {
        // Given
        when(positionRepository.existsByName(testPositionRequestDto.getName())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.create(testPositionRequestDto)
        );

        assertEquals("Vị trí đã tồn tại", exception.getMessage()); // Cập nhật message
        verify(positionRepository).existsByName(testPositionRequestDto.getName());
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo position với lỗi database constraint nên throw IllegalArgumentException")
    void createShouldThrowIllegalArgumentExceptionOnDataIntegrityViolation() {
        // Given
        when(positionRepository.existsByName(testPositionRequestDto.getName())).thenReturn(false);
        when(positionMapper.requestDtoToEntity(testPositionRequestDto)).thenReturn(testPosition);
        when(positionRepository.save(testPosition)).thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.create(testPositionRequestDto)
        );

        assertEquals("Không thể tạo vị trí vì ràng buộc dữ liệu", exception.getMessage());
        verify(positionRepository).save(testPosition);
    }

    @Test
    @DisplayName("Cập nhật position thành công")
    void updateShouldReturnUpdatedPosition() {
        // Given
        PositionRequestDto updateDto = PositionRequestDto.builder()
                .name("Senior Software Engineer")
                .description("Updated Description")
                .build();

        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionRepository.existsByName(updateDto.getName())).thenReturn(false);
        when(positionRepository.save(testPosition)).thenReturn(testPosition);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        PositionResponseDto result = positionService.update(testPosition.getId(), updateDto);

        // Then
        assertNotNull(result);
        verify(positionRepository).findById(testPosition.getId());
        verify(positionRepository).save(testPosition);
        verify(positionMapper).updateEntityFromDto(updateDto, testPosition);
    }

    @Test
    @DisplayName("Cập nhật position với tên đã tồn tại nên throw exception")
    void updateShouldThrowExceptionWhenNameExists() {
        // Given
        PositionRequestDto updateDto = PositionRequestDto.builder()
                .name("Existing Position")
                .description("Updated Description")
                .build();

        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionRepository.existsByName(updateDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.update(testPosition.getId(), updateDto)
        );

        assertEquals("Vị trí với tên này đã tồn tại", exception.getMessage());
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật position không tồn tại nên throw exception")
    void updateShouldThrowExceptionWhenPositionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(positionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.update(nonExistentId, testPositionRequestDto)
        );

        assertEquals("Không tìm thấy vị trí với ID: " + nonExistentId, exception.getMessage());
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa position thành công")
    void deleteShouldRemovePosition() {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(employmentDetailRepository.countByPositionId(testPosition.getId())).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> positionService.delete(testPosition.getId()));

        // Then
        verify(positionRepository).existsById(testPosition.getId());
        verify(employmentDetailRepository).countByPositionId(testPosition.getId());
        verify(positionRepository).deleteById(testPosition.getId());
    }

    @Test
    @DisplayName("Xóa position không tồn tại nên throw exception")
    void deleteShouldThrowExceptionWhenPositionNotFound() {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.delete(testPosition.getId())
        );

        assertEquals("Không tìm thấy vị trí với ID: " + testPosition.getId(), exception.getMessage());
        verify(positionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Xóa position đang được sử dụng nên throw exception")
    void deleteShouldThrowExceptionWhenPositionInUse() {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(employmentDetailRepository.countByPositionId(testPosition.getId())).thenReturn(3L);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> positionService.delete(testPosition.getId())
        );

        assertEquals("Không thể xóa vị trí vì vẫn còn 3 nhân viên thuộc vị trí này", exception.getMessage());
        verify(positionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Lấy position với members thành công")
    void getPositionWithMembersShouldReturnPositionAndMembers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<PositionEntity> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(testPosition));

        when(positionRepository.findPositionWithMembers(testPosition.getId(), pageable))
                .thenReturn(mockPage);
        when(positionMapper.entityToDtoWithMembers(testPosition, pageable))
                .thenReturn(new HashMap<>());

        // When
        Map<String, Object> result = positionService.getPositionWithMembers(testPosition.getId(), pageable);

        // Then
        assertNotNull(result);
        verify(positionRepository).findPositionWithMembers(testPosition.getId(), pageable);
        verify(positionMapper).entityToDtoWithMembers(testPosition, pageable);
    }

    @Test
    @DisplayName("Lấy position với members khi position không tồn tại nên throw exception")
    void getPositionWithMembersShouldThrowExceptionWhenPositionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PositionEntity> emptyPage = mock(Page.class);
        when(emptyPage.getContent()).thenReturn(Collections.emptyList());

        when(positionRepository.findPositionWithMembers(nonExistentId, pageable))
                .thenReturn(emptyPage);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.getPositionWithMembers(nonExistentId, pageable)
        );

        assertEquals("Không tìm thấy vị trí với ID: " + nonExistentId, exception.getMessage());
        verify(positionMapper, never()).entityToDtoWithMembers(any(), any());
    }
}