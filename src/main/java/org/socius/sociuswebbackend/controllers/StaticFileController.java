package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.StaticFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/static")
@RequiredArgsConstructor
public class StaticFileController {

    private static final Logger logger = LoggerFactory.getLogger(StaticFileController.class);

    private final StaticFileService staticFileService;

    @GetMapping("/{directory}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String directory,
            @PathVariable String filename) {

        try {
            // Kiểm tra đầu vào cơ bản
            if (!isValidInput(directory, filename)) {
                return ResponseEntity.badRequest().build();
            }

            // Ủy thác cho service xử lý logic phức tạp
            Resource resource = staticFileService.serveStaticFile(directory, filename);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // Lấy content type từ service
            String contentType = staticFileService.getContentType(directory, filename);
            String cleanFilename = StringUtils.cleanPath(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + cleanFilename + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi serve file: {}/{} - {}",
                    directory, filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Kiểm tra đầu vào cơ bản tại tầng controller
     */
    private boolean isValidInput(String directory, String filename) {
        return directory != null && !directory.trim().isEmpty()
                && filename != null && !filename.trim().isEmpty();
    }
}