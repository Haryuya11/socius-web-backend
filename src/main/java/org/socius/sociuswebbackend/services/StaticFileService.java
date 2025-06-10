package org.socius.sociuswebbackend.services;

import org.springframework.core.io.Resource;

public interface StaticFileService {

    /**
     * Phục vụ file tĩnh với các kiểm tra bảo mật
     *
     * @param directory thư mục chứa file
     * @param filename tên file
     * @return Resource của file hoặc null nếu không hợp lệ
     */
    Resource serveStaticFile(String directory, String filename);

    /**
     * Lấy content type của file
     *
     * @param directory thư mục
     * @param filename tên file
     * @return content type
     */
    String getContentType(String directory, String filename);

    /**
     * Kiểm tra file có tồn tại và hợp lệ không
     *
     * @param directory thư mục
     * @param filename tên file
     * @return true nếu hợp lệ
     */
    boolean isValidFile(String directory, String filename);
}