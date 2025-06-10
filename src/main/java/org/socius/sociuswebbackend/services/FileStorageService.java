package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.file.FileMetadataDto;
import org.socius.sociuswebbackend.model.enums.MessageType;
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
     * Lấy đường dẫn đầy đủ của tệp tin
     *
     * @param fileUrl URL tương đối của tệp tin
     * @return đường dẫn đầy đủ của tệp tin
     */
    Path getFilePath(String fileUrl);

    /**
     * Xóa tệp tin
     *
     * @param fileUrl URL của tệp tin cần xóa
     * @throws IOException nếu có lỗi khi xóa tệp tin
     */
    void deleteFile(String fileUrl) throws IOException;

    /**
     * Lấy metadata của tệp tin
     *
     * @param fileUrl URL của tệp tin
     * @return metadata của tệp tin
     * @throws IOException nếu có lỗi khi đọc metadata
     */
    FileMetadataDto getFileMetadata(String fileUrl) throws IOException;

    /**
     * Xác định loại tin nhắn dựa trên tệp tin
     *
     * @param file tệp tin upload
     * @return loại tin nhắn tương ứng
     */
    MessageType determineMessageType(MultipartFile file);

    /**
     * Xác định thư mục lưu trữ dựa trên loại tin nhắn
     *
     * @param messageType loại tin nhắn
     * @return tên thư mục lưu trữ
     */
    String determineDirectory(MessageType messageType);
}