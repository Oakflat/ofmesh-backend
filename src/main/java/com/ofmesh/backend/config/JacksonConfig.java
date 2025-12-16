package com.ofmesh.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // ✅ 自动注册模块（关键：让 LocalDateTime/Instant 正常序列化）
        // 需要 classpath 有 jackson-datatype-jsr310（一般 spring-boot-starter-web 会带）
        mapper.findAndRegisterModules();

        // ✅ 统一 UTC
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        // ✅ 强制输出 ISO-8601 字符串，不输出时间戳数字
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
