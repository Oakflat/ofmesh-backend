package com.ofmesh.backend.security;

import com.ofmesh.backend.entity.AccountStatus;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import com.ofmesh.backend.exception.AccountBannedException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginKey) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(loginKey)
                .or(() -> userRepository.findByEmail(loginKey))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            if (user.getBanUntil() == null || user.getBanUntil().isAfter(now)) {
                throw new AccountBannedException(user.getBanUntil(), user.getBanReason());
            }

            // ✅ 到期：顺手收敛（可选）
            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setBanUntil(null);
            user.setBanReason(null);
            userRepository.save(user);
        }

        return user;
    }
}