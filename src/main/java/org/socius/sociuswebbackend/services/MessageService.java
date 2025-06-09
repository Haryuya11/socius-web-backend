package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MessageService {

    /**
     * Gửi tin nhắn mới
     *
     * @param requestDto Thông tin tin nhắn
     * @return Thông tin tin nhắn đã gửi
     */
    MessageResponseDto sendMessage(MessageRequestDto requestDto);

    /**
     * Lấy tin nhắn trong cuộc trò chuyện với phân trang
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param pageable       Thông tin phân trang
     * @return Trang tin nhắn
     */
    Page<MessageResponseDto> getMessages(UUID conversationId, Pageable pageable);

    /**
     * Đánh dấu tin nhắn đã đọc
     *
     * @param readReceiptDto Thông tin về tin nhắn cuối cùng đã đọc
     * @return Số tin nhắn được đánh dấu là đã đọc
     */
    int markAsRead(ReadReceiptDto readReceiptDto);

    /**
     * Đồng bộ tin nhắn mới sau khi mất kết nối
     *
     * @param syncRequest Thông tin đồng bộ
     * @return Danh sách tin nhắn đã đồng bộ
     */
    Map<UUID, List<MessageResponseDto>> syncMessages(SyncMessagesRequestDto syncRequest);

    /**
     * Xóa tin nhắn
     *
     * @param messageId ID của tin nhắn cần xóa
     */
    void deleteMessage(UUID messageId);

    /**
     * Cập nhật tin nhắn
     *
     * @param messageId  ID của tin nhắn cần cập nhật
     * @param requestDto Thông tin cập nhật
     * @return Thông tin tin nhắn đã cập nhật
     */
    MessageResponseDto updateMessage(UUID messageId, MessageRequestDto requestDto);


    /**
     * Tìm kiếm tin nhắn trong một cuộc trò chuyện theo từ khóa
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param keyword        Từ khóa tìm kiếm
     * @param pageable       Thông tin phân trang
     * @return Trang các tin nhắn tìm thấy
     */
    Page<MessageResponseDto> searchMessages(UUID conversationId, String keyword, Pageable pageable);

}
