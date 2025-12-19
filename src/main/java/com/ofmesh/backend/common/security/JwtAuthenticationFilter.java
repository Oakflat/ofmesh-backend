package com.ofmesh.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.common.web.dto.ApiErrorResponse;
import com.ofmesh.backend.common.exception.AccountBannedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    // ✅ 注入 ObjectMapper，用来输出结构化 JSON
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1) 没 token 或格式不对：放行（后续由 security 拦截）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        final String username;
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // token 解析失败，放行交给后续处理（一般会 401）
            filterChain.doFilter(request, response);
            return;
        }

        // 2) 有用户名 & 当前未认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 3) token 校验通过才设置认证
                if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {

                    // ✅ 兜底：如果不是通过 AccountBannedException，而是别的禁用逻辑导致 disabled/locked
                    if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                        writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                new ApiErrorResponse("FORBIDDEN", "账号已被封禁或禁用"));
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (AccountBannedException ex) {
                // ✅ 关键：封禁时稳定 403 + 结构化信息
                SecurityContextHolder.clearContext();
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        new ApiErrorResponse("ACCOUNT_BANNED", "账号已被封禁", ex.getBanUntil(), ex.getBanReason()));
                return;

            } catch (DisabledException ex) {
                // ✅ 兜底：其他 disabled
                SecurityContextHolder.clearContext();
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        new ApiErrorResponse("FORBIDDEN", "账号已被禁用或锁定"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
