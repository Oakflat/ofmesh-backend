package com.ofmesh.backend.security;

import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
// @RequiredArgsConstructor ❌ 删掉它
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // ✅ 手动添加构造函数
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 因为我们的 User 实体已经实现了 UserDetails 接口，直接返回即可
        return user;
    }
}