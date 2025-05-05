package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.AppSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppSettingsRepository extends JpaRepository<AppSettingsEntity, UUID> {
    /**
     * Tìm kiếm cấu hình hệ thống theo khóa cấu hình
     * @param settingKey Khóa cấu hình
     * @return Optional<AppSettingsEntity> nếu tìm thấy, Optional.empty() nếu không tìm thấy
     */
    Optional<AppSettingsEntity> findBySettingKey(String settingKey);

    /**
     * Kiểm tra xem cấu hình hệ thống có tồn tại hay không
     * @param settingKey Khóa cấu hình
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    boolean existsBySettingKey(String settingKey);

    /**
     * Xóa cấu hình hệ thống theo khóa cấu hình
     * @param settingKey Khóa cấu hình
     */
    void deleteBySettingKey(String settingKey);
}
