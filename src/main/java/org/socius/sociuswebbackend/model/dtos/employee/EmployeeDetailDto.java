package org.socius.sociuswebbackend.model.dtos.employee;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentHistoryResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryHistoryResponseDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeDetailDto extends BaseDto {
    // Thông tin cá nhân và công việc
    private EmploymentDetailResponseDto employmentDetail;
    
    // Thông tin hiệu suất
    private BigDecimal averagePerformanceRating;
    private Integer completedTasks;
    private Integer pendingTasks;
    private Integer failedTasks;
    private BigDecimal peerScore;
    private Integer positivePeerVotes;
    private Integer negativePeerVotes;
    
    // Lịch sử
    private List<EmploymentHistoryResponseDto> employmentHistory;
    private List<SalaryHistoryResponseDto> salaryHistory;
}
