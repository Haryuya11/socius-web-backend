package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.file.FileMetadataDto;
import org.socius.sociuswebbackend.security.FileValidator;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    final private ConfigService configService;
    final private FileValidator fileValidator;


    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) throw new IOException("Không thể lưu tệp tin rỗng");

        // Kiểm tra thư mục đích có hợp lệ không
        if (!fileValidator.isAllowedDirectory(directory)) {
            throw new IOException("Thư mục không hợp lệ: " + directory);
        }

        // Kiểm tra loại tệp tin
        if (!fileValidator.isAllowedFileType(file.getContentType())) {
            throw new IOException("Loại tệp tin không hợp lệ: " + file.getContentType());
        }

        // Kiểm tra kích thước tệp tin
        if (!fileValidator.isAllowedFileSize(file.getSize())) {
            throw new IOException("Kích thước tệp tin vượt quá giới hạn cho phép: " + file.getSize());
        }

        // Làm sạch tên file và phần mở rộng
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = fileValidator.getCleanFileExtension(originalFilename);

        // Tạo tên file duy nhất
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // Tạo đường dẫn lưu trữ
        String baseDir = configService.getString("file.upload.dir", "./uploads");
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path uploadPath = basePath.resolve(directory).normalize();

        // Tạo thư mục nếu không tồn tại
        Files.createDirectories(uploadPath);


        // Xây dựng đường dẫn đích
        Path targetLocation = uploadPath.resolve(uniqueFileName).normalize();

        // Kiểm tra xem đường dẫn đích có nằm trong thư mục cơ sở
        if (!fileValidator.isSecurePath(basePath, targetLocation)) {
            throw new IOException("Đường dẫn tệp tin không hợp lệ: Vượt ra ngoài thư mục cho phép");
        }

//        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        try {
            byte[] fileBytes = file.getBytes();

            // Kiểm tra nội dung tệp tin
            if (!fileValidator.isValidFileContent(fileBytes, file.getContentType())) {
                throw new IOException("Nội dung tệp tin không phù hợp với loại tệp khai báo");
            }

            // Ghi file sử dụng Files.write thay vì Files.copy để tránh
            Files.write(targetLocation, fileBytes);

            // Lưu metadata vào cơ sở dữ liệu
            saveFileMetadata(uploadPath, uniqueFileName, originalFilename, file);

            return directory + "/" + uniqueFileName;
        } catch (IOException e) {
            logger.error("Lỗi khi lưu tệp tin: {}", e.getMessage());
            throw new IOException("Lỗi khi lưu tệp tin: " + e.getMessage(), e);
        }
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

    @Override
    public FileMetadataDto getFileMetadata(String fileUrl) throws IOException {
        Path path = Paths.get(configService.getString("file.upload.dir", "./uploads"), fileUrl + ".meta.json");
        if (!Files.exists(path)) {
            logger.error("Không tìm thấy metadata cho tệp tin: {}", fileUrl);
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(path.toFile(), FileMetadataDto.class);
    }

    private void saveFileMetadata(Path uploadPath, String uniqueFileName, String originalFilename, MultipartFile file) throws IOException {
        Map<String, Object> metadata = Map.of(
                "originalFilename", originalFilename,
                "contentType", Objects.requireNonNull(file.getContentType()),
                "size", file.getSize(),
                "uploadDate", LocalDateTime.now().toString()
        );

        Path metadataPath = uploadPath.resolve(uniqueFileName + ".meta.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(metadataPath.toFile(), metadata);
    }

}
