package org.socius.sociuswebbackend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    /**
     * Lưu trữ tệp tin
     *
     * @param file      tệp tin cần lưu
     * @param directory thư mục lưu trữ
     * @return đường dẫn URL của tệp tin đã lưu
     * @throws IOException nếu có lỗi xảy ra trong quá trình lưu trữ
     */
    String storeFile(MultipartFile file, String directory) throws IOException;

    /**
     * Lấy đường dẫn tệp tin từ URL
     *
     * @param fileUrl URL của tệp tin
     * @return đường dẫn tệp tin
     */
    Path getFilePath(String fileUrl);

    /**
     * Xóa tệp tin
     *
     * @param fileUrl URL của tệp tin cần xóa
     * @throws IOException nếu có lỗi xảy ra trong quá trình xóa
     */
    void deleteFile(String fileUrl) throws IOException;
}