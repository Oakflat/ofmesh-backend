package com.ofmesh.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilderCustomizer;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer offsetDateTimeCustomizer() {
        return builder -> builder
                // ✅ 让 OffsetDateTime / LocalDateTime 等 Java Time 能被正确序列化
                .modules(new JavaTimeModule())
                // ✅ 输出 ISO-8601 字符串，而不是时间戳
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // ✅ 统一使用 UTC 时区（和你 DB 的 UTC 时间轴一致）
                .timeZone(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).build();
    }
}
