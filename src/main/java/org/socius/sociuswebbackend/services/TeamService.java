package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    /**
     * Lấy danh sách tất cả các team
     *
     * @return Danh sách các team
     */
    List<TeamResponseDto> findAll();

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
     * Thêm nhân viên vào team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần thêm
     * @return Thông tin team sau khi thêm nhân viên
     */
    TeamResponseDto addEmployee(UUID teamId, UUID employeeId);

    /**
     * Thêm nhiều nhân viên vào team
     *
     * @param teamId      ID của team
     * @param employeeIds Danh sách ID của các nhân viên cần thêm
     * @return Danh sách thông tin team sau khi thêm nhân viên
     */
    List<TeamResponseDto> addEmployees(UUID teamId, List<UUID> employeeIds);

    /**
     * Xóa nhân viên khỏi team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần xóa
     * @return Thông tin team sau khi xóa nhân viên
     */
    TeamResponseDto removeEmployee(UUID teamId, UUID employeeId);

    /**
     * Xóa nhiều nhân viên khỏi team
     *
     * @param teamId      ID của team
     * @param employeeIds Danh sách ID của các nhân viên cần xóa
     * @return Danh sách thông tin team sau khi xóa nhân viên
     */
    List<TeamResponseDto> removeEmployees(UUID teamId, List<UUID> employeeIds);

}