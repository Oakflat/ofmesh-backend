package com.ofmesh.backend.user.auth.service;

import com.ofmesh.backend.user.auth.dto.LoginRequest;
import com.ofmesh.backend.user.auth.dto.PasswordResetRequest;
import com.ofmesh.backend.user.auth.dto.RegisterRequest;
import com.ofmesh.backend.user.profile.entity.Role;
import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import com.ofmesh.backend.common.security.JwtUtil;
import com.ofmesh.backend.common.utils.IpUtil;
import com.ofmesh.backend.common.mail.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.AuthenticationException;

import com.ofmesh.backend.common.exception.AccountBannedException;

import com.ofmesh.backend.user.profile.entity.AccountStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    //  注入 Request 用于获取 IP
    private final HttpServletRequest request;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       EmailService emailService,
                       StringRedisTemplate redisTemplate,
                       HttpServletRequest request) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.request = request;
    }

    // ==========================================
    // 1. 发送验证码 (含：60秒限流 + IP防刷 + 全局熔断)
    // ==========================================
    public void sendVerificationCode(String email, String type) {

        // ---  0. 检查全局熔断 (Circuit Breaker) ---
        // 如果触发了熔断，6小时内所有邮件请求直接拒绝
        if (Boolean.TRUE.equals(redisTemplate.hasKey("email:circuit_breaker"))) {
            throw new RuntimeException("系统邮件服务暂时繁忙 (熔断保护中)，请 6 小时后再试");
        }

        // ---  1. 单 IP 每日限流 (防止单人刷爆) ---
        String ip = IpUtil.getIpAddress(request);
        String today = LocalDate.now().toString(); // "2025-12-12"
        String ipLimitKey = "email:limit:ip:" + today + ":" + ip;

        // 限制单个 IP 每天最多发 20 条
        Long ipCount = redisTemplate.opsForValue().increment(ipLimitKey);
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipLimitKey, 24, TimeUnit.HOURS); // 首次创建设置过期时间
        }
        if (ipCount != null && ipCount > 20) {
            throw new RuntimeException("您今日获取验证码次数已达上限");
        }

        // ---  2. 全局每日总量检查 (2000条熔断) ---
        String globalLimitKey = "email:limit:global:" + today;
        Long globalCount = redisTemplate.opsForValue().increment(globalLimitKey);
        if (globalCount != null && globalCount == 1) {
            redisTemplate.expire(globalLimitKey, 24, TimeUnit.HOURS);
        }

        // 触发熔断逻辑：超过 2000 条，拉闸 6 小时
        if (globalCount != null && globalCount > 2000) {
            redisTemplate.opsForValue().set("email:circuit_breaker", "1", 6, TimeUnit.HOURS);
            throw new RuntimeException("系统邮件配额已耗尽，服务暂停 6 小时");
        }

        // --- 3. 常规 60秒 冷却 ---
        String rateLimitKey = "rate_limit:email:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long expire = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            throw new RuntimeException("发送太频繁，请 " + expire + " 秒后再试");
        }

        // --- 4. 业务逻辑检查 ---
        boolean exists = userRepository.existsByEmail(email);
        if ("register".equals(type)) {
            if (exists) throw new RuntimeException("该邮箱已被注册，请直接登录");
        } else if ("reset".equals(type)) {
            if (!exists) throw new RuntimeException("该邮箱尚未注册");
        } else {
            throw new RuntimeException("未知的验证类型");
        }

        // --- 5. 生成与发送 ---
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        String verifyKey = "verify:" + type + ":" + email;

        // 存入验证码 (5分钟有效)
        redisTemplate.opsForValue().set(verifyKey, code, 5, TimeUnit.MINUTES);

        // 开启 60s 冷却 (设置冷却 Key)
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);

        System.out.println(">>> [Auth] 向 " + email + " 发送验证码: " + code + " [IP: " + ip + "]");
        emailService.sendVerificationCode(email, code);
    }

    // ==========================================
    // 2. 注册业务
    // ==========================================
    @Transactional
    public String register(RegisterRequest request) {
        String email = request.getEmail();
        String redisKey = "verify:register:" + email;
        String cachedCode = redisTemplate.opsForValue().get(redisKey);

        if (cachedCode == null) throw new RuntimeException("验证码已过期或未获取");
        if (!cachedCode.equals(request.getCode())) throw new RuntimeException("验证码错误");

        redisTemplate.delete(redisKey); // 验证成功后删除，防止复用

        if (userRepository.existsByUsername(request.getUsername())) throw new RuntimeException("用户名已存在");
        if (userRepository.existsByEmail(email)) throw new RuntimeException("邮箱已被注册");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername());
    }

    // ==========================================
    // 3. 登录业务
    // ==========================================
    public String login(LoginRequest request) {
        String key = request.getLoginKey();
        if (key == null || key.isBlank()) throw new RuntimeException("账号不能为空");
        if (request.getPassword() == null || request.getPassword().isBlank()) throw new RuntimeException("密码不能为空");

        //  0) 封禁优先：不管密码是否正确，先查库判断是否仍在封禁期
        User preUser = userRepository.findByUsername(key)
                .or(() -> userRepository.findByEmail(key))
                .orElse(null);

        if (preUser != null && preUser.getAccountStatus() == AccountStatus.BANNED) {
            OffsetDateTime until = preUser.getBanUntil();
            boolean stillBanned = (until == null) || until.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
            if (stillBanned) {
                throw new AccountBannedException(preUser.getBanUntil(), preUser.getBanReason());
            }
        }

        //  1) 再走认证（这时剩下的失败就是 BAD_CREDENTIALS）
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(key, request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new RuntimeException("账号或密码错误");
        }

        User user = userRepository.findByUsername(key)
                .or(() -> userRepository.findByEmail(key))
                .orElseThrow(() -> new RuntimeException("账号或密码错误"));

        return jwtUtil.generateToken(user.getUsername());
    }


    // ==========================================
    // 4. 重置密码业务
    // ==========================================
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String email = request.getEmail();
        String redisKey = "verify:reset:" + email;
        String cachedCode = redisTemplate.opsForValue().get(redisKey);

        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        redisTemplate.delete(redisKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}