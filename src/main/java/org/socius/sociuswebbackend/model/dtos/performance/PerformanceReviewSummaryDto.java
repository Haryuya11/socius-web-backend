package org.socius.sociuswebbackend.model.dtos.performance;

import lombok.Data;
import org.socius.sociuswebbackend.model.enums.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PerformanceReviewSummaryDto {
    private UUID employeeId;
    private String employeeName;
    private String employeeImageUrl;
    private UUID periodId;
    private String periodName;
    private PeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal averageRating;
    private int reviewCount;
    private BigDecimal peerScore;
    private int positiveVotes;
    private int negativeVotes;
    private int completedTasks;
    private int failedTasks;
}
