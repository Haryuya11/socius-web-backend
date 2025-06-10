package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TeamService {

    /**
     * Lấy danh sách tất cả các team
     *
     * @return Danh sách các team
     */
    List<TeamResponseDto> findAll();

    /**
     * Lấy danh sách tất cả các team đang hoạt động
     *
     * @return Danh sách các team đang hoạt động
     */
    List<TeamResponseDto> findAllActiveTeams();

    /**
     * Tìm một team theo ID
     *
     * @param id ID của team cần tìm
     * @return team nếu tìm thấy, null nếu không tìm thấy
     */
    TeamResponseDto findById(UUID id);

    /**
     * Tạo một team mới
     *
     * @param requestDto Thông tin yêu cầu tạo team
     * @return Thông tin team đã được tạo
     */
    TeamResponseDto create(TeamRequestDto requestDto);

    /**
     * Cập nhật thông tin một team
     *
     * @param id         ID của team cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật team
     * @return Thông tin team đã được cập nhật
     */
    TeamResponseDto update(UUID id, TeamRequestDto requestDto);

    /**
     * Xóa một team
     *
     * @param id ID của team cần xóa
     */
    void delete(UUID id);

    /**
     * Lấy thông tin team cùng với các thành viên của nó (không bao gồm task)
     *
     * @param teamId ID của team cần lấy thông tin
     * @param pageable Thông tin phân trang
     * @return Map chứa thông tin team và danh sách thành viên
     */
    Map<String, Object> getTeamWithMembers(UUID teamId, Pageable pageable);

    /**
     * Lấy danh sách task của một team theo ID
     *
     * @param teamId   ID của team cần lấy danh sách task
     * @param pageable Thông tin phân trang (số trang, kích thước trang)
     * @return Map chứa danh sách task, tổng số task, số trang, và tổng phần tử
     */
    Map<String, Object> getTasksByTeamId(UUID teamId, Pageable pageable);
}