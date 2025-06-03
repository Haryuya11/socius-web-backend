package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.Map;

public interface TaskService {



    /**
     * Tạo một task mới
     *
     * @param taskRequestDto Đối tượng chứa thông tin task cần tạo
     * @return Thông tin task vừa tạo
     */
    TaskResponseDto createTask(TaskRequestDto taskRequestDto);

    /**
     * Cập nhật trạng thái của một task
     *
     * @param taskId ID của task cần cập nhật
     * @param status Trạng thái mới của task
     * @return Thông tin task đã cập nhật
     */
    TaskResponseDto updateTaskStatus(UUID taskId, String status);

    /**
     * Kiểm tra và cập nhật trạng thái các task quá hạn
     * <p>
     * Phương thức này chạy định kỳ để đặt trạng thái FAILED cho các task
     * đã quá hạn mà chưa hoàn thành
     */
    void checkAndUpdateOverdueTasks();
}
