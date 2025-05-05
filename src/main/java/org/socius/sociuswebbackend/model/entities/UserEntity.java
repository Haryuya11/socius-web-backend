package org.socius.sociuswebbackend.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "First name must not be empty")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name must not be empty")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email is not valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "birth_date", nullable = false)
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @NotNull(message = "Hire date must not be null")
    @Column(name = "hire_date", nullable = false)
    @PastOrPresent(message = "Hire date must be today or in the past")
    private LocalDate hireDate;

    @Column(name = "address")
    private String address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private AccountEntity account;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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

    @OneToOne(mappedBy = "leader", fetch = FetchType.LAZY)
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

    @PrePersist
    @PreUpdate
    void validateAge() {
        if (birthDate != null && birthDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }
    }
}
