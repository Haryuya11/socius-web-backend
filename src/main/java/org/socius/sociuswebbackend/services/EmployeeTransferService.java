package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;

import java.util.UUID;

public interface EmployeeTransferService {
    /**
     * Chuyển nhân viên sang phòng ban mới
     *
     * @param employeeId      ID của nhân viên cần chuyển
     * @param newDepartmentId ID của phòng ban mới
     * @return Thông tin chi tiết về việc làm sau khi chuyển
     */
    EmploymentDetailResponseDto transferDepartment(UUID employeeId, UUID newDepartmentId);

    /**
     * Chuyển nhân viên sang team mới
     *
     * @param employeeId ID của nhân viên cần chuyển
     * @param newTeamId  ID của team mới
     * @return Thông tin chi tiết về việc làm sau khi chuyển
     */
    EmploymentDetailResponseDto transferTeam(UUID employeeId, UUID newTeamId);

    /**
     * Chuyển nhân viên sang vị trí mới
     *
     * @param employeeId    ID của nhân viên cần chuyển
     * @param newPositionId ID của vị trí mới
     * @return Thông tin chi tiết về việc làm sau khi chuyển
     */
    EmploymentDetailResponseDto transferPosition(UUID employeeId, UUID newPositionId);
}
