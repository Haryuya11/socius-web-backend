package org.socius.sociuswebbackend.services.impl;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.entities.AppSettingsEntity;
import org.socius.sociuswebbackend.repositories.AppSettingsRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final static Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    @Autowired
    private Environment environment;

    @Value("${app.cookie.name:SOCIUS_SESSION}")
    private String defaultCookieName;

    @Value("${app.session.attribute.user_id:USER_ID}")
    private String defaultUserIdAttribute;

    @Override
    @Cacheable(value = "configCache", key = "#key")
    public String getString(String key) {
        // Kiểm tra application.properties trước
        String value = environment.getProperty("app." + key);
        if (value != null) {
            return value;
        }

        // Nếu không có trong application.properties, tìm trong database
        return appSettingsRepository.findBySettingKey(key)
                .map(AppSettingsEntity::getSettingValue)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "configCache", key = "#key")
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public int getInt(String key) {
        String value = getString(key);
        if (value == null) {
            logger.warn("Không tìm thấy cấu hình cho key: {}, trả về 0", key);
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Lỗi chuyển đổi giá trị [{}] thành số nguyên cho key: {}", value, key, e);
            return 0;
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Lỗi chuyển đổi giá trị [{}] thành số nguyên cho key: {}, sử dụng giá trị mặc định: {}",
                    value, key, defaultValue, e);
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String key) {
        String value = getString(key);
        return value != null && Boolean.parseBoolean(value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @Override
    public List<String> getList(String key) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    @Override
    public Map<String, String> getAllConfigs() {
        return appSettingsRepository.findAll().stream()
                .collect(Collectors.toMap(AppSettingsEntity::getSettingKey, AppSettingsEntity::getSettingValue));
    }

    @Override
    @Transactional
    @CacheEvict(value = "configCache", key = "#key")
    public void setString(String key, String value) {
        setString(key, value, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configCache", key = "#key")
    public void setString(String key, String value, String description) {
        Optional<AppSettingsEntity> configOptional = appSettingsRepository.findBySettingKey(key);
        AppSettingsEntity config = configOptional.orElseGet(() -> {
            AppSettingsEntity newConfig = new AppSettingsEntity();
            newConfig.setSettingKey(key);
            return newConfig;
        });

        config.setSettingValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        appSettingsRepository.save(config);
        logger.info("Đã cập nhật cấu hình: {} = {}", key, value);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configCache", key = "#key")
    public void setInt(String key, int value) {
        setString(key, String.valueOf(value));
    }

    @Override
    @Transactional
    @CacheEvict(value = "configCache", key = "#key")
    public void setBoolean(String key, boolean value) {
        setString(key, String.valueOf(value));
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = environment.getProperty(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public String getSetting(String key, String defaultValue) {
        return appSettingsRepository.findBySettingKey(key)
                .map(AppSettingsEntity::getSettingValue)
                .orElse(defaultValue);
    }
}