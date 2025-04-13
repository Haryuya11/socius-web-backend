package org.socius.sociuswebbackend.model.dtos.search;

import lombok.Data;
import org.socius.sociuswebbackend.model.enums.Gender;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
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
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "lastName";
    private Boolean ascending = true;
}
