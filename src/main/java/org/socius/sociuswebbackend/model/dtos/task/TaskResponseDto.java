package org.socius.sociuswebbackend.model.dtos.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.TaskStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TaskResponseDto extends BaseDto {
    private String name;
    private String description;
    private LocalDate deadline;
    private TaskStatus status;
    private UserResponseDto assignedTo;
}
