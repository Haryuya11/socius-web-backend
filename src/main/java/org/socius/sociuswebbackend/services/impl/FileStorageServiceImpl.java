package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    final private ConfigService configService;

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) throw new IOException("Không thể lưu tệp tin rỗng");

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";

        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID() + fileExtension;
        String baseDir = configService.getString("file.upload.dir", "./uploads");
        Path uploadPath = Paths.get(baseDir, directory).toAbsolutePath().normalize();

        // Tạo thư mục nếu không tồn tại
        Files.createDirectories(uploadPath);

        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Đã lưu tệp tin: {} tại {}", uniqueFileName, targetLocation);

        return directory + "/" + uniqueFileName;
    }

    @Override
    public Path getFilePath(String fileUrl) {
        String baseDir = configService.getString("file.upload.dir", "./uploads");
        Path filePath = Paths.get(baseDir, fileUrl).toAbsolutePath().normalize();

        if (!Files.exists(filePath)) {
            logger.error("Tệp tin không tồn tại: {}", filePath);
            return null;
        }

        return filePath;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        Path filePath = getFilePath(fileUrl);
        if (filePath != null && Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("Đã xóa tệp tin: {}", filePath);
        } else {
            logger.warn("Không thể xóa tệp tin, tệp không tồn tại: {}", fileUrl);
        }
    }
}
