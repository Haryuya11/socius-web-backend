package org.socius.sociuswebbackend.model.dtos.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.Gender;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeSearchCriteriaDto {
    private String name;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private UUID departmentId;
    private UUID positionId;
    private UUID teamId;
    private UUID roleId;
    private WorkingStatus workingStatus;
    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private BigDecimal performanceFrom;
    private BigDecimal performanceTo;
    private Boolean hasActiveTasks;
    private Boolean isTeamLeader;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
    @Builder.Default
    private String sortBy = "lastName";
    @Builder.Default
    private Boolean ascending = true;
}
