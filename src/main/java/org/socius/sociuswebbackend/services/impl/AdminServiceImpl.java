package org.socius.sociuswebbackend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.UserMapper;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.AdminService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private EmploymentDetailRepository employmentDetailRepository;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfigService configService;

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
        UserEntity user = new UserEntity();
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setBirthDate(requestDto.getBirthDate());
        user.setGender(requestDto.getGender());
        user.setNationality(requestDto.getNationality());
        user.setPhoneNumber(requestDto.getPhoneNumber());
        user.setAddress(requestDto.getAddress());
        user.setHireDate(requestDto.getHireDate());
        
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
        account = accountRepository.save(account);
        
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