package com.ofmesh.backend.common.config;

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
        ObjectMapper mapper = new ObjectMapper();

        // ✅ 支持 Java Time（OffsetDateTime / LocalDateTime）
        mapper.registerModule(new JavaTimeModule());

        // ✅ 使用 ISO-8601，而不是时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ 统一 UTC（和 DB 时间轴一致）
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        return mapper;
    }
}
