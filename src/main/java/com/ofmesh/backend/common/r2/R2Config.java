package com.ofmesh.backend.common.r2;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    public StaticCredentialsProvider r2Credentials(R2Properties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );
    }

    @Bean
    public S3Client r2S3Client(R2Properties props, StaticCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                // R2 常用做法：endpointOverride + 任意 region（用 us-east-1 最稳）
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                // R2 很多场景 path-style 更稳（尤其你截图 S3 API URL 带 /bucket）
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    public S3Presigner r2Presigner(R2Properties props, StaticCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
