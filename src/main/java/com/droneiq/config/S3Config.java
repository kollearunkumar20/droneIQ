package com.droneiq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "true")
public class S3Config {

    private static final Logger log = LoggerFactory.getLogger(S3Config.class);

    @Value("${storage.s3.region:us-east-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        log.info("Configuring AWS S3Client in region: {}", region);
        return S3Client.builder()
                .region(Region.of(region))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
