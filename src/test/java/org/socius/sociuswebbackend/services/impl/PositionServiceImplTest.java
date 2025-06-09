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
import org.socius.sociuswebbackend.mappers.PositionMappingHelper;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.PositionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PositionServiceImplTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private PositionMappingHelper positionMappingHelper;

    @InjectMocks
    private PositionServiceImpl positionService;

    private PositionEntity testPosition;
    private PositionRequestDto testPositionRequestDto;
    private PositionResponseDto testPositionResponseDto;

    @BeforeEach
    void setUp() {
        testPosition = PositionEntity.builder()
                .id(UUID.randomUUID())
                .name("Software Engineer")
                .description("Develop and maintain software applications")
                .build();

        testPositionRequestDto = PositionRequestDto.builder()
                .name("Software Engineer")
                .description("Develop and maintain software applications")
                .build();

        testPositionResponseDto = PositionResponseDto.builder()
                .id(testPosition.getId())
                .name("Software Engineer")
                .description("Develop and maintain software applications")
                .build();
    }

    @Test
    @DisplayName("Lấy danh sách tất cả positions thành công")
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
    @DisplayName("Lấy danh sách positions đang hoạt động thành công")
    void findAllActivePositionsShouldReturnActivePositions() {
        // Given
        List<PositionEntity> activePositions = Collections.singletonList(testPosition);
        when(positionRepository.findAllActivePositions()).thenReturn(activePositions);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        List<PositionResponseDto> result = positionService.findAllActivePositions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPositionResponseDto.getName(), result.getFirst().getName());
        verify(positionRepository).findAllActivePositions();
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
    @DisplayName("Tìm position theo ID không tồn tại sẽ ném exception")
    void findByIdWithNonExistentIdShouldThrowException() {
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
    void createPositionShouldSucceed() {
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
        verify(positionMapper).requestDtoToEntity(testPositionRequestDto);
        verify(positionRepository).save(testPosition);
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Tạo position với tên đã tồn tại sẽ ném exception")
    void createPositionWithExistingNameShouldThrowException() {
        // Given
        when(positionRepository.existsByName(testPositionRequestDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.create(testPositionRequestDto)
        );

        assertEquals("Vị trí với tên này đã tồn tại", exception.getMessage());
        verify(positionRepository).existsByName(testPositionRequestDto.getName());
        verify(positionRepository, never()).save(any());
        verify(positionMapper, never()).requestDtoToEntity(any());
        verify(positionMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Tạo position với DataIntegrityViolationException sẽ ném IllegalArgumentException")
    void createPositionWithDataIntegrityViolationShouldThrowIllegalArgumentException() {
        // Given
        when(positionRepository.existsByName(testPositionRequestDto.getName())).thenReturn(false);
        when(positionMapper.requestDtoToEntity(testPositionRequestDto)).thenReturn(testPosition);
        when(positionRepository.save(testPosition)).thenThrow(new DataIntegrityViolationException("DB error"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.create(testPositionRequestDto)
        );

        assertEquals("Không thể tạo vị trí vì ràng buộc dữ liệu", exception.getMessage());
        verify(positionRepository).existsByName(testPositionRequestDto.getName());
        verify(positionMapper).requestDtoToEntity(testPositionRequestDto);
        verify(positionRepository).save(testPosition);
        verify(positionMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Cập nhật position thành công")
    void updatePositionShouldSucceed() {
        // Given
        PositionRequestDto updateRequest = PositionRequestDto.builder()
                .name("Senior Software Engineer")
                .description("Lead development team")
                .build();

        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(positionRepository.save(testPosition)).thenReturn(testPosition);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        PositionResponseDto result = positionService.update(testPosition.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPositionResponseDto.getName(), result.getName());
        verify(positionRepository).findById(testPosition.getId());
        verify(positionRepository).existsByName(updateRequest.getName());
        verify(positionMapper).updateEntityFromDto(updateRequest, testPosition);
        verify(positionRepository).save(testPosition);
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Cập nhật position không tồn tại sẽ ném exception")
    void updateNonExistentPositionShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(positionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.update(nonExistentId, testPositionRequestDto)
        );

        assertEquals("Không tìm thấy vị trí với ID: " + nonExistentId, exception.getMessage());
        verify(positionRepository).findById(nonExistentId);
        verify(positionRepository, never()).existsByName(any());
        verify(positionMapper, never()).updateEntityFromDto(any(), any());
        verify(positionRepository, never()).save(any());
        verify(positionMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Cập nhật position với tên đã tồn tại sẽ ném exception")
    void updatePositionWithExistingNameShouldThrowException() {
        // Given
        PositionRequestDto updateRequest = PositionRequestDto.builder()
                .name("Existing Position")
                .description("Description")
                .build();

        testPosition.setName("Original Position"); // Set tên khác với updateRequest
        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionRepository.existsByName(updateRequest.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.update(testPosition.getId(), updateRequest)
        );

        assertEquals("Vị trí với tên này đã tồn tại", exception.getMessage());
        verify(positionRepository).findById(testPosition.getId());
        verify(positionRepository).existsByName(updateRequest.getName());
        verify(positionMapper, never()).updateEntityFromDto(any(), any());
        verify(positionRepository, never()).save(any());
        verify(positionMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Cập nhật position với cùng tên hiện tại thành công")
    void updatePositionWithSameNameShouldSucceed() {
        // Given
        PositionRequestDto updateRequest = PositionRequestDto.builder()
                .name("Software Engineer") // Cùng tên với position hiện tại
                .description("Updated description")
                .build();

        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));
        when(positionRepository.save(testPosition)).thenReturn(testPosition);
        when(positionMapper.entityToDto(testPosition)).thenReturn(testPositionResponseDto);

        // When
        PositionResponseDto result = positionService.update(testPosition.getId(), updateRequest);

        // Then
        assertNotNull(result);
        verify(positionRepository).findById(testPosition.getId());
        verify(positionRepository, never()).existsByName(any()); // Không check existsByName vì tên giống nhau
        verify(positionMapper).updateEntityFromDto(updateRequest, testPosition);
        verify(positionRepository).save(testPosition);
        verify(positionMapper).entityToDto(testPosition);
    }

    @Test
    @DisplayName("Xóa position thành công")
    void deletePositionShouldSucceed() {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(employmentDetailRepository.countByPositionId(testPosition.getId())).thenReturn(0L);
        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));

        // When
        assertDoesNotThrow(() -> positionService.delete(testPosition.getId()));

        // Then
        verify(positionRepository).existsById(testPosition.getId());
        verify(employmentDetailRepository).countByPositionId(testPosition.getId());
        verify(positionRepository).findById(testPosition.getId());
        verify(positionRepository).save(testPosition);
    }

    @Test
    @DisplayName("Xóa position không tồn tại sẽ ném exception")
    void deleteNonExistentPositionShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(positionRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> positionService.delete(nonExistentId)
        );

        assertEquals("Không tìm thấy vị trí với ID: " + nonExistentId, exception.getMessage());
        verify(positionRepository).existsById(nonExistentId);
        verify(employmentDetailRepository, never()).countByPositionId(any());
        verify(positionRepository, never()).findById(any());
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa position còn nhân viên sẽ ném exception")
    void deletePositionWithEmployeesShouldThrowException() {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(employmentDetailRepository.countByPositionId(testPosition.getId())).thenReturn(3L);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> positionService.delete(testPosition.getId())
        );

        assertEquals("Không thể xóa vị trí vì vẫn còn 3 nhân viên thuộc vị trí này", exception.getMessage());
        verify(positionRepository).existsById(testPosition.getId());
        verify(employmentDetailRepository).countByPositionId(testPosition.getId());
        verify(positionRepository, never()).findById(any());
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lấy position với thành viên thành công")
    void getPositionWithMembersShouldSucceed() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // Mock Page với PositionEntity
        Page<PositionEntity> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(testPosition));

        Map<String, Object> mockResponse = createMockPositionWithMembersResponse();

        when(positionRepository.findPositionWithMembers(testPosition.getId(), pageable))
                .thenReturn(mockPage);
        when(positionMappingHelper.entityToDtoWithMembers(testPosition))
                .thenReturn(mockResponse);

        // When
        Map<String, Object> result = positionService.getPositionWithMembers(testPosition.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("memberCount"));
        assertEquals(Collections.emptyList(), result.get("members"));
        verify(positionRepository).findPositionWithMembers(testPosition.getId(), pageable);
        verify(positionMappingHelper).entityToDtoWithMembers(testPosition);
    }

    @Test
    @DisplayName("Lấy position với thành viên khi position không tồn tại sẽ ném exception")
    void getPositionWithMembersForNonExistentPositionShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // Mock Page rỗng
        Page<PositionEntity> emptyPage = mock(Page.class);
        when(emptyPage.getContent()).thenReturn(Collections.emptyList());
        when(positionRepository.findPositionWithMembers(nonExistentId, pageable))
                .thenReturn(emptyPage);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> positionService.getPositionWithMembers(nonExistentId, pageable)
        );

        assertEquals("Position not found with ID: " + nonExistentId, exception.getMessage());
        verify(positionRepository).findPositionWithMembers(nonExistentId, pageable);
        verify(positionMappingHelper, never()).entityToDtoWithMembers(any());
    }

    @Test
    @DisplayName("Lấy position với thành viên khi repository trả về null sẽ ném exception")
    void getPositionWithMembersWhenRepositoryReturnsNullShouldThrowException() {
        // Given
        UUID positionId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(positionRepository.findPositionWithMembers(positionId, pageable))
                .thenReturn(null);

        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> positionService.getPositionWithMembers(positionId, pageable)
        );

        assertNotNull(exception.getMessage());
        verify(positionRepository).findPositionWithMembers(positionId, pageable);
        verify(positionMappingHelper, never()).entityToDtoWithMembers(any());
    }

    private Map<String, Object> createMockPositionWithMembersResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("id", testPosition.getId());
        response.put("name", testPosition.getName());
        response.put("description", testPosition.getDescription());
        response.put("members", Collections.emptyList());
        response.put("memberCount", 0);
        return response;
    }
}