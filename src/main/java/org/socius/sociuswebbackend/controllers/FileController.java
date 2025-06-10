package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.file.FileMetadataDto;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.ConversationMemberRepository;
import org.socius.sociuswebbackend.repositories.ConversationRepository;
import org.socius.sociuswebbackend.repositories.MessageRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    final private FileStorageService fileStorageService;
    final private MessageRepository messageRepository;
    final private UserRepository userRepository;
    final private ConversationRepository conversationRepository;
    final private ConversationMemberRepository conversationMemberRepository;

    @GetMapping("/{messageId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID messageId) {
        try {
            MessageEntity message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId));

            if (message.getFileUrl() == null || message.getFileUrl().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fileStorageService.getFilePath(message.getFileUrl()).toUri());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Lấy metadata của file
            FileMetadataDto fileMetadata = fileStorageService.getFileMetadata(message.getFileUrl());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMetadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Lỗi khi tải xuống file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
