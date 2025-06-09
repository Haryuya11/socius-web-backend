package org.socius.sociuswebbackend.model.dtos.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.enums.Gender;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto extends BaseDto {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String imageUrl;
    private Gender gender;
    private String nationality;
    private String phoneNumber;
    private LocalDate hireDate;
    private String address;
}
