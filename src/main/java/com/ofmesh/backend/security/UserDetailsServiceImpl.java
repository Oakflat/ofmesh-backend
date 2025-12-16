package com.ofmesh.backend.security;

import com.ofmesh.backend.entity.AccountStatus;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

        // 封禁判断：永久封禁（banUntil=null）或未到期封禁，都直接拒绝
        if (user.getAccountStatus() == AccountStatus.BANNED) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (user.getBanUntil() == null || user.getBanUntil().isAfter(now)) {
                throw new DisabledException("账号已封禁: " + (user.getBanReason() == null ? "" : user.getBanReason()));
            }
        }

        return user;
    }
}
