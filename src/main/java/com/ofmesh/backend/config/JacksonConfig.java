package com.ofmesh.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();

        // ✅ 让 OffsetDateTime / LocalDateTime 等 Java Time 能被正确序列化
        om.registerModule(new JavaTimeModule());

        // ✅ 输出 ISO-8601 字符串，而不是时间戳
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ 统一使用 UTC 时区（和你 DB 的 UTC 时间轴一致）
        om.setTimeZone(TimeZone.getTimeZone("UTC"));

        return om;
    }
}
