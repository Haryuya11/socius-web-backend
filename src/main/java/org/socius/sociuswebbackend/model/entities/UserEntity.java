package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"account", "employmentDetail", "employmentHistories", "salaryHistories",
        "assignedTasks", "assignedTargets", "receivedReviews", "givenReviews",
        "givenVotes", "receivedVotes", "sentNotifications", "receivedNotifications",
        "rankings", "ledTeam", "loginHistories"})
public class UserEntity extends BaseEntity {

    @NotBlank(message = "Họ không được để trống")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "birth_date", nullable = false)
    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate birthDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại không hợp lệ")
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "address")
    private String address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private AccountEntity account;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private EmploymentDetailEntity employmentDetail;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<EmploymentHistoryEntity> employmentHistories = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<SalaryHistoryEntity> salaryHistories = new HashSet<>();

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<TaskEntity> assignedTasks = new HashSet<>();

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<TargetEntity> assignedTargets = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PerformanceReviewEntity> receivedReviews = new HashSet<>();

    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PerformanceReviewEntity> givenReviews = new HashSet<>();

    @OneToMany(mappedBy = "voter", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PeerVoteEntity> givenVotes = new HashSet<>();

    @OneToMany(mappedBy = "votedEmployee", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PeerVoteEntity> receivedVotes = new HashSet<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<NotificationEntity> sentNotifications = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<NotificationRecipientEntity> receivedNotifications = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<EmployeeRankingEntity> rankings = new HashSet<>();

    @OneToOne(mappedBy = "leader")
    @JsonIgnore
    private TeamEntity ledTeam;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<LoginHistoryEntity> loginHistories = new HashSet<>();

    @Transient
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    protected void validateEntity() {
        validateAge();
    }

    void validateAge() {
        if (birthDate != null && birthDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }
    }
}
