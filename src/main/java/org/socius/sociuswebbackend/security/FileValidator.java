package org.socius.sociuswebbackend.security;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FileValidator {

    final private ConfigService configService;

    /**
     * Kiểm tra xem tệp tin có hợp lệ hay không
     *
     * @param contentType loại nội dung của tệp tin
     * @return true nếu tệp tin hợp lệ, false nếu không
     */
    public boolean isAllowedFileType(String contentType) {
        // Kiểm tra định dạng tệp tin
        List<String> allowedTypes = Arrays.asList(
                "image/jpeg", "image/png", "image/gif", "application/pdf",
                "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "audio/mpeg", "video/mp4", "text/plain");
        return allowedTypes.contains(contentType);
    }

    /**
     * Kiểm tra xem kích thước tệp tin có hợp lệ hay không
     *
     * @param fileSize kích thước tệp tin
     * @return true nếu kích thước tệp tin hợp lệ, false nếu không
     */
    public boolean isAllowedFileSize(long fileSize) {
        long maxFileSize = configService.getLong("file.upload.max.size", 52428800); // 50MB
        return fileSize <= maxFileSize;
    }

    /**
     * Kiểm tra tên file và đường dẫn có hợp lệ hay không
     *
     * @param basePath đường dẫn gốc
     * @param filePath đường dẫn tệp tin
     * @return true nếu đường dẫn hợp lệ, false nếu không
     */
    public boolean isSecurePath(Path basePath, Path filePath) {
        try {
            return filePath.normalize().toAbsolutePath().startsWith(basePath.normalize().toAbsolutePath());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra xem thư mục có được phép hay không
     *
     * @param directory tên thư mục
     * @return true nếu thư mục được phép, false nếu không
     */
    public boolean isAllowedDirectory(String directory) {
        // Danh sách các thư mục được phép
        Set<String> allowedDirectories = Set.of("images", "videos", "documents", "audios", "files");
        return allowedDirectories.contains(directory);
    }

    /**
     * Làm sạch phần mở rộng của file
     *
     * @param originalFilename tên file gốc
     * @return tên file đã được làm sạch
     */
    public String getCleanFileExtension(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Chỉ giữ lại các ký tự hợp lệ
            extension = extension.replaceAll("[^a-zA-Z0-9.]", "");
            // Giới hạn độ dài
            extension = extension.length() > 10 ? extension.substring(0, 10) : extension;
        }
        return extension;
    }

    /**
     * Kiểm tra xem nội dung file có khớp với loại file khai báo không
     *
     * @param fileBytes   byte array của file
     * @param contentType loại nội dung khai báo
     * @return true nếu nội dung file hợp lệ với loại khai báo, false nếu không
     */
    public boolean isValidFileContent(byte[] fileBytes, String contentType) {
        if (fileBytes.length < 4) {
            return false; // File quá nhỏ không thể xác định magic number
        }

        // Kiểm tra magic number của các loại file phổ biến
        if (contentType.equals("image/jpeg")) {
            // JPEG: FF D8 FF
            return fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8 && fileBytes[2] == (byte) 0xFF;
        } else if (contentType.equals("image/png")) {
            // PNG: 89 50 4E 47
            return fileBytes[0] == (byte) 0x89 && fileBytes[1] == 'P' && fileBytes[2] == 'N' && fileBytes[3] == 'G';
        } else if (contentType.equals("application/pdf")) {
            // PDF: 25 50 44 46 (% P D F)
            return fileBytes[0] == '%' && fileBytes[1] == 'P' && fileBytes[2] == 'D' && fileBytes[3] == 'F';
        } else if (contentType.equals("audio/mpeg")) {
            // MP3: FF FB or FF F3 or FF F2 or ID3
            return (fileBytes[0] == (byte) 0xFF && (fileBytes[1] == (byte) 0xFB || fileBytes[1] == (byte) 0xF3 || fileBytes[1] == (byte) 0xF2)) ||
                    (fileBytes[0] == 'I' && fileBytes[1] == 'D' && fileBytes[2] == '3');
        } else if (contentType.equals("video/mp4")) {
            // MP4:.... ftyp
            if (fileBytes.length < 12) return false;
            return (fileBytes[4] == 'f' && fileBytes[5] == 't' && fileBytes[6] == 'y' && fileBytes[7] == 'p');
        }

        // Đối với các loại tệp khác (ví dụ: text/plain, các tệp office...),
        // không có magic number đơn giản, hãy cân nhắc sử dụng thư viện như Apache Tika nếu cần

        // Cho các loại file khác, chấp nhận mặc định
        return true;
    }
}
