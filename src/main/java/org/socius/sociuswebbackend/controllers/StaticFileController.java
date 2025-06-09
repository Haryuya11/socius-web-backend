package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/static")
@RequiredArgsConstructor
public class StaticFileController {

    private static final Logger logger = LoggerFactory.getLogger(StaticFileController.class);
    private final ConfigService configService;

    @GetMapping("/{directory}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String directory,
            @PathVariable String filename) {
        try {
            // Kiểm tra directory có hợp lệ không
            if (!isAllowedDirectory(directory)) {
                return ResponseEntity.notFound().build();
            }

            String baseDir = configService.getString("file.upload.dir", "./uploads");
            Path filePath = Paths.get(baseDir, directory, filename).toAbsolutePath().normalize();

            if (!Files.exists(filePath)) {
                logger.warn("File không tồn tại: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Xác định content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Lỗi khi serve file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isAllowedDirectory(String directory) {
        return directory.equals("images") ||
                directory.equals("videos") ||
                directory.equals("documents") ||
                directory.equals("audios") ||
                directory.equals("files");
    }
}