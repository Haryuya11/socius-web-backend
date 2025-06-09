package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.AppSettingMapper;
import org.socius.sociuswebbackend.model.dtos.config.AppSettingUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.config.ConfigDto;
import org.socius.sociuswebbackend.repositories.AppSettingsRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
@RequiredArgsConstructor
public class ConfigController {

    final private ConfigService configService;
    final private AppSettingsRepository appSettingsRepository;
    final private AppSettingMapper configMapper;

    /**
     * Lấy danh sách cấu hình
     *
     * @return Danh sách cấu hình
     */
    @GetMapping
    public ResponseEntity<List<ConfigDto>> getAllConfigs() {
        List<ConfigDto> configs = appSettingsRepository.findAll().stream()
                .map(configMapper::entityToDto)
                .toList();
        return ResponseEntity.ok(configs);
    }

    /**
     * Lấy cấu hình theo key
     *
     * @param key Khóa của cấu hình cần lấy
     * @return Cấu hình tương ứng với key
     */
    @GetMapping("/{key}")
    public ResponseEntity<ConfigDto> getConfigByKey(@PathVariable String key) {
        return appSettingsRepository.findBySettingKey(key)
                .map(entity -> ResponseEntity.ok(configMapper.entityToDto(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cập nhật cấu hình theo key
     *
     * @param key     Khóa của cấu hình cần cập nhật
     * @param request Thông tin yêu cầu cập nhật cấu hình
     * @return Cấu hình đã được cập nhật
     */
    @PostMapping("/{key}")
    public ResponseEntity<ConfigDto> updateConfig(@PathVariable String key, @Valid @RequestBody AppSettingUpdateRequestDto request) {
        configService.setString(key, request.getValue(), request.getDescription());

        return appSettingsRepository.findBySettingKey(key)
                .map(entity -> ResponseEntity.ok(configMapper.entityToDto(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo mới cấu hình theo key
     *
     * @param key     Khóa của cấu hình cần tạo
     * @param request Thông tin yêu cầu tạo cấu hình
     * @return Cấu hình đã được tạo
     */
    @PutMapping("/{key}")
    public ResponseEntity<ConfigDto> createConfig(@PathVariable String key, @Valid @RequestBody AppSettingUpdateRequestDto request) {
        if (appSettingsRepository.existsBySettingKey(key)) {
            return ResponseEntity.badRequest().build();
        }

        configService.setString(key, request.getValue(), request.getDescription());

        return appSettingsRepository.findBySettingKey(key)
                .map(entity -> ResponseEntity.ok(configMapper.entityToDto(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa cấu hình theo key
     *
     * @param key Khóa của cấu hình cần xóa
     * @return HTTP 204 No Content nếu xóa thành công, hoặc HTTP 404 Not Found nếu không tìm thấy cấu hình
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String key) {
        if (!appSettingsRepository.existsBySettingKey(key)) {
            return ResponseEntity.notFound().build();
        }

        appSettingsRepository.deleteBySettingKey(key);
        return ResponseEntity.noContent().build();
    }
}
