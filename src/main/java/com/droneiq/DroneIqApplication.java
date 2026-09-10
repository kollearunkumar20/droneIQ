package com.droneiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DroneIqApplication {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DroneIqApplication.class);

    public static void main(String[] args) {
        configureCloudDatabaseUrl();
        SpringApplication.run(DroneIqApplication.class, args);
    }

    /**
     * Automatically parses standard cloud platform DATABASE_URL (Render, Railway, Heroku)
     * from postgres:// or postgresql:// format and maps it to Spring Boot datasource properties.
     */
    static void configureCloudDatabaseUrl() {
        parseAndApplyDatabaseUrl(System.getenv("DATABASE_URL"));
    }

    static void parseAndApplyDatabaseUrl(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            String normalizedUrl = databaseUrl.trim();
            if (normalizedUrl.startsWith("postgres://")) {
                normalizedUrl = "postgresql://" + normalizedUrl.substring("postgres://".length());
            }

            java.net.URI uri = new java.net.URI(normalizedUrl);
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length > 0 && System.getProperty("spring.datasource.username") == null) {
                    System.setProperty("spring.datasource.username", parts[0]);
                }
                if (parts.length > 1 && System.getProperty("spring.datasource.password") == null) {
                    System.setProperty("spring.datasource.password", parts[1]);
                }
            }

            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String host = uri.getHost();
            String path = uri.getPath(); // includes leading '/'

            if (host != null && path != null) {
                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append(path);

                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl.append("?").append(uri.getQuery());
                }

                if (System.getProperty("spring.datasource.url") == null) {
                    System.setProperty("spring.datasource.url", jdbcUrl.toString());
                    log.info("Auto-configured spring.datasource.url from cloud DATABASE_URL for host: {}", host);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse DATABASE_URL environment variable: {}", e.getMessage());
        }
    }
}
