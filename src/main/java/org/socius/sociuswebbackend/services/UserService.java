package org.socius.sociuswebbackend.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    /**
     * Tìm người dùng theo ID
     *
     * @param userId ID của người dùng cần tìm
     * @return Thông tin người dùng hoặc null nếu không tìm thấy
     */
    UserResponseDto findById(UUID userId);

    /**
     * Lấy thông tin người dùng hiện tại từ request
     *
     * @param request Request HTTP hiện tại
     * @return Thông tin người dùng hiện tại hoặc null nếu không có phiên hợp lệ
     */
    EmploymentDetailResponseDto getCurrentUser(HttpServletRequest request);


    /**
     * Lấy danh sách người dùng không thuộc bất kỳ team nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ team nào
     */
    List<UserResponseDto> getActiveUsersNotInAnyTeam(HttpServletRequest request);

    /**
     * Lấy danh sách người dùng theo phân trang
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách người dùng cùng với thông tin phân trang
     */
    Map<String, Object> getEmploymentHistory(UUID userId, Pageable pageable);

    /**
     * Lấy lịch sử lương của người dùng theo ID
     *
     * @param userId   ID của người dùng cần lấy lịch sử lương
     * @param pageable Thông tin phân trang
     * @return Danh sách lịch sử lương cùng với thông tin phân trang
     */
    Map<String, Object> getSalaryHistory(UUID userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết về việc làm của người dùng theo ID
     *
     * @param userId ID của người dùng cần lấy thông tin việc làm
     * @return Thông tin chi tiết về việc làm của người dùng
     */
    Map<String, Object> getEmploymentDetailByUserId(UUID userId);

    /**
     * Lấy danh sách người dùng không thuộc bất kỳ phòng ban nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ phòng ban nào
     */
    List<UserResponseDto> getActiveUsersNotInAnyDepartment(HttpServletRequest request);

    /**
     * Lấy danh sách người dùng không thuộc bất kỳ vị trí nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ vị trí nào
     */
    List<UserResponseDto> getActiveUsersNotInAnyPosition(HttpServletRequest request);

    /**
     * Lấy danh sách task của một người dùng theo ID
     *
     * @param userId   ID của người dùng cần lấy danh sách task
     * @param pageable Thông tin phân trang (số trang, kích thước trang)
     * @return Map chứa danh sách task, tổng số task, số trang, và tổng phần tử
     */
    Map<String, Object> getTasksByUserId(UUID userId, Pageable pageable);

    /**
     * Cập nhật thông tin người dùng
     *
     * @param userID         ID của người dùng cần cập nhật
     * @param userRequestDto Thông tin người dùng mới
     * @return Thông tin người dùng đã cập nhật
     */
    UserResponseDto updateInfoUser(UUID userID, UserRequestDto userRequestDto);
}