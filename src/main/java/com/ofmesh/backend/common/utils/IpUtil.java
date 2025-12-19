package com.ofmesh.backend.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    /**
     * 获取用户真实 IP 地址
     * 优先处理 Cloudflare 和 Nginx 转发的情况
     */
    public static String getIpAddress(HttpServletRequest request) {
        // Cloudflare 传递的真实 IP Header
        String ip = request.getHeader("CF-Connecting-IP");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于多级代理的情况 "1.1.1.1, 2.2.2.2"，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}