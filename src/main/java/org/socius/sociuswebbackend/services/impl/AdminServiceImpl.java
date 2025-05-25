package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.UserMapper;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.AdminService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    final private UserRepository userRepository;
    final private AccountRepository accountRepository;
    final private PositionRepository positionRepository;
    final private DepartmentRepository departmentRepository;
    final private TeamRepository teamRepository;
    final private RoleRepository roleRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private UserMapper userMapper;
    final private PasswordEncoder passwordEncoder;
    final private ConfigService configService;

    @Override
    @Transactional
    public UserResponseDto createEmployee(EmployeeCreationRequestDto requestDto) {
        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email này đã được sử dụng");
        }

        // Kiểm tra các thực thể tham chiếu
        Optional<PositionEntity> positionOpt = positionRepository.findById(requestDto.getPositionId());
        if (positionOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy vị trí");
        }

        Optional<DepartmentEntity> departmentOpt = departmentRepository.findById(requestDto.getDepartmentId());
        if (departmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy phòng ban");
        }

        TeamEntity team = null;
        if (requestDto.getTeamId() != null) {
            Optional<TeamEntity> teamOpt = teamRepository.findById(requestDto.getTeamId());
            if (teamOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy team");
            }
            team = teamOpt.get();
        }

        Optional<RoleEntity> roleOpt = roleRepository.findById(requestDto.getRoleId());
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy vai trò");
        }

        // Tạo người dùng mới
        UserEntity user = UserEntity.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .birthDate(requestDto.getBirthDate())
                .gender(requestDto.getGender())
                .nationality(requestDto.getNationality())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .hireDate(requestDto.getHireDate())
                .build();

        // Lưu người dùng
        user = userRepository.save(user);
        String defaultPassword = configService.getString("default.password", "1");

        // Tạo tài khoản với mật khẩu mặc định
        AccountEntity account = new AccountEntity();
        account.setUser(user);
        account.setPassword(passwordEncoder.encode(defaultPassword));
        account.setIsActive(true);
        account.setIsDefaultPassword(true);

        // Lưu tài khoản
        accountRepository.save(account);

        // Tạo chi tiết việc làm
        EmploymentDetailEntity employmentDetail = new EmploymentDetailEntity();
        employmentDetail.setUser(user);
        employmentDetail.setPosition(positionOpt.get());
        employmentDetail.setDepartment(departmentOpt.get());
        employmentDetail.setTeam(team);
        employmentDetail.setRole(roleOpt.get());
        employmentDetail.setStartDate(requestDto.getHireDate());
        employmentDetail.setSalary(requestDto.getSalary());
        employmentDetail.setWorkingStatus(requestDto.getWorkingStatus());

        // Lưu chi tiết việc làm
        employmentDetailRepository.save(employmentDetail);

        // Trả về thông tin người dùng đã tạo
        return userMapper.entityToDto(user);
    }
}