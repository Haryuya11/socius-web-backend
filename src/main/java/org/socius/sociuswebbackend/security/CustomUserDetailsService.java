package org.socius.sociuswebbackend.security;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.AccountRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    final private UserRepository userRepository;

    final private AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm kiếm người dùng bằng email
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
        }

        UserEntity user = userOptional.get();

        // Lấy thông tin từ người dùng
        Optional<AccountEntity> accountOptional = accountRepository.findByUser(user);
        if (accountOptional.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản với người dùng: " + user.getId());
        }

        AccountEntity account = accountOptional.get();

        // Kiểm tra trạng thái tài khoản
        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản không hoạt động: " + email);
        }

        // Lấy thông tin quyền hạn
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getEmploymentDetail() != null && user.getEmploymentDetail().getRole() != null) {
            user.getEmploymentDetail().getRole().getRolePermissions().forEach(rolePermission -> {
                if (rolePermission.getPermission() != null) {
                    authorities.add(new SimpleGrantedAuthority(rolePermission.getPermission().getName()));
                }
            });
        }
        return new User(
                user.getEmail(),
                account.getPassword(),
                account.getIsActive(),
                true, // Tài khoản không hết hạn
                true, // Mật khẩu không hết hạn
                true, // Tài khoản không bị khóa
                authorities);

    }
}
