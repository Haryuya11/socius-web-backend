package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.repositories.ConversationMemberRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationService conversationService;

    @DeleteMapping("/{conversationId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID conversationId,
            @PathVariable UUID memberId,
            HttpServletRequest request) {

        UUID currentUserId = getCurrentUserId(request);

        // Kiểm tra quyền admin của user hiện tại
        ConversationMemberEntity currentMember = conversationMemberRepository
                .findActiveMember(conversationId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên của cuộc trò chuyện"));

        if (currentMember.getRole() != MemberRole.ADMIN) {
            throw new RuntimeException("Chỉ admin mới có thể xóa thành viên");
        }

        conversationService.removeMember(conversationId, memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy ID của user hiện tại từ session
     *
     * @param request HTTP request
     * @return UUID của user hiện tại
     */
    private UUID getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new RuntimeException("Phiên đăng nhập không hợp lệ");
        }

        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng trong phiên");
        }

        return userId;
    }
}