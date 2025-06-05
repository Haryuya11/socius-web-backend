package org.socius.sociuswebbackend.model.dtos.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTerminationResponseDto {
    private List<UUID> successfulEmployeeIds;
    private List<UUID> failedEmployeeIds;
    private LocalDateTime terminationDate;
    private int totalProcessed;
    private int successCount;
    private int failureCount;
}