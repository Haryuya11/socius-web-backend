package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.security.FileValidator;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.StaticFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StaticFileServiceImpl implements StaticFileService {

    private static final Logger logger = LoggerFactory.getLogger(StaticFileServiceImpl.class);

    private final ConfigService configService;
    private final FileValidator fileValidator;

    @Override
    public Resource serveStaticFile(String directory, String filename) {
        try {
            // 1. Làm sạch và kiểm tra tham số đầu vào
            String cleanDirectory = StringUtils.cleanPath(directory);
            String cleanFilename = StringUtils.cleanPath(filename);

            // 2. Kiểm tra directory có hợp lệ không
            if (!fileValidator.isAllowedDirectory(cleanDirectory)) {
                logger.warn("Directory không hợp lệ: {}", cleanDirectory);
                return null;
            }

            // 3. Kiểm tra filename không chứa ký tự nguy hiểm
            if (!isSecureFilename(cleanFilename)) {
                logger.warn("Filename không hợp lệ: {}", cleanFilename);
                return null;
            }

            // 4. Xây dựng đường dẫn file
            Path filePath = buildSecureFilePath(cleanDirectory, cleanFilename);
            if (filePath == null) {
                return null;
            }

            // 5. Kiểm tra file có tồn tại và có thể đọc không
            if (!Files.exists(filePath)) {
                logger.warn("File không tồn tại: {}", filePath);
                return null;
            }

            // 6. Tạo và kiểm tra Resource
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                logger.warn("File không thể đọc: {}", filePath);
                return null;
            }

            // 7. Kiểm tra content type có hợp lệ không
            String contentType = Files.probeContentType(filePath);
            if (contentType != null && !fileValidator.isAllowedFileType(contentType)) {
                logger.warn("Content type không được phép: {}", contentType);
                return null;
            }

            return resource;

        } catch (Exception e) {
            logger.error("Lỗi khi serve file: {}/{} - {}", directory, filename, e.getMessage());
            return null;
        }
    }

    @Override
    public String getContentType(String directory, String filename) {
        try {
            Path filePath = buildSecureFilePath(
                    StringUtils.cleanPath(directory),
                    StringUtils.cleanPath(filename)
            );

            if (filePath != null && Files.exists(filePath)) {
                String contentType = Files.probeContentType(filePath);
                return contentType != null ? contentType : "application/octet-stream";
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xác định content type: {}", e.getMessage());
        }
        return "application/octet-stream";
    }

    @Override
    public boolean isValidFile(String directory, String filename) {
        String cleanDirectory = StringUtils.cleanPath(directory);
        String cleanFilename = StringUtils.cleanPath(filename);

        return fileValidator.isAllowedDirectory(cleanDirectory)
                && isSecureFilename(cleanFilename)
                && buildSecureFilePath(cleanDirectory, cleanFilename) != null;
    }

    /**
     * Kiểm tra filename có an toàn không
     */
    private boolean isSecureFilename(String filename) {
        return !filename.contains("..")
                && !filename.contains("/")
                && !filename.contains("\\")
                && !filename.trim().isEmpty();
    }

    /**
     * Xây dựng đường dẫn file an toàn
     */
    private Path buildSecureFilePath(String directory, String filename) {
        try {
            String baseDir = configService.getString("file.upload.dir", "./uploads");
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Path directoryPath = basePath.resolve(directory).normalize();
            Path filePath = directoryPath.resolve(filename).normalize();

            // Kiểm tra Path Traversal
            if (!fileValidator.isSecurePath(basePath, filePath)) {
                logger.warn("Path traversal attack detected: {}", filePath);
                return null;
            }

            return filePath;
        } catch (Exception e) {
            logger.error("Lỗi khi xây dựng đường dẫn file: {}", e.getMessage());
            return null;
        }
    }
}