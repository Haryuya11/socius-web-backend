package org.socius.sociuswebbackend.services;

import java.util.List;
import java.util.Map;

public interface ConfigService {
    /**
     * Lấy giá trị cấu hình dạng chuỗi
     *
     * @param key Khóa cấu hình
     * @return Giá trị cấu hình
     */
    String getString(String key);

    /**
     * Lấy giá trị cấu hình dạng chuỗi với giá trị mặc định
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định nếu không tìm thấy
     * @return Giá trị cấu hình hoặc giá trị mặc định
     */
    String getString(String key, String defaultValue);

    /**
     * Lấy giá trị cấu hình dạng số nguyên
     *
     * @param key Khóa cấu hình
     * @return Giá trị cấu hình
     */
    int getInt(String key);

    /**
     * Lấy giá trị cấu hình dạng số nguyên với giá trị mặc định
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định nếu không tìm thấy hoặc không phải số
     * @return Giá trị cấu hình hoặc giá trị mặc định
     */
    int getInt(String key, int defaultValue);

    /**
     * Lấy giá trị cấu hình dạng số thực
     *
     * @param key Khóa cấu hình
     * @return Giá trị cấu hình
     */
    double getDouble(String key);

    /**
     * Lấy giá trị cấu hình dạng số thực với giá trị mặc định
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định nếu không tìm thấy hoặc không phải số
     * @return Giá trị cấu hình hoặc giá trị mặc định
     */
    double getDouble(String key, double defaultValue);

    /**
     * Lấy giá trị cấu hình dạng số nguyên dài
     *
     * @param key Khóa cấu hình
     * @return Giá trị cấu hình
     */
    long getLong(String key);

    /**
     * Lấy giá trị cấu hình dạng số nguyên dài với giá trị mặc định
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định nếu không tìm thấy hoặc không phải số
     * @return Giá trị cấu hình hoặc giá trị mặc định
     */
    long getLong(String key, long defaultValue);

    /**
     * Lấy giá trị cấu hình dạng boolean
     *
     * @param key Khóa cấu hình
     * @return Giá trị cấu hình
     */
    boolean getBoolean(String key);

    /**
     * Lấy giá trị cấu hình dạng boolean với giá trị mặc định
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định nếu không tìm thấy
     * @return Giá trị cấu hình hoặc giá trị mặc định
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Lấy danh sách giá trị được phân tách bằng dấu phẩy
     *
     * @param key Khóa cấu hình
     * @return Danh sách các giá trị
     */
    List<String> getList(String key);

    /**
     * Lấy tất cả cấu hình từ database
     *
     * @return Map chứa tất cả cấu hình
     */
    Map<String, String> getAllConfigs();

    /**
     * Cập nhật giá trị cấu hình
     *
     * @param key   Khóa cấu hình
     * @param value Giá trị mới
     */
    void setString(String key, String value);

    /**
     * Cập nhật giá trị cấu hình với mô tả
     *
     * @param key         Khóa cấu hình
     * @param value       Giá trị mới
     * @param description Mô tả
     */
    void setString(String key, String value, String description);

    /**
     * Cập nhật giá trị cấu hình số nguyên
     *
     * @param key   Khóa cấu hình
     * @param value Giá trị mới
     */
    void setInt(String key, int value);

    /**
     * Cập nhật giá trị cấu hình boolean
     *
     * @param key   Khóa cấu hình
     * @param value Giá trị mới
     */
    void setBoolean(String key, boolean value);

    /**
     * Lấy giá trị cấu hình từ file application.properties
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định
     * @return Giá trị từ application.properties hoặc giá trị mặc định
     */
    String getProperty(String key, String defaultValue);

    /**
     * Lấy giá trị cấu hình từ bảng app_settings
     *
     * @param key          Khóa cấu hình
     * @param defaultValue Giá trị mặc định
     * @return Giá trị từ bảng app_settings hoặc giá trị mặc định
     */
    String getSetting(String key, String defaultValue);

}