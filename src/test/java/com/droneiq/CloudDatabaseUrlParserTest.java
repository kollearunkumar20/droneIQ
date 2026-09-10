package com.droneiq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloudDatabaseUrlParserTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("spring.datasource.url");
        System.clearProperty("spring.datasource.username");
        System.clearProperty("spring.datasource.password");
    }

    @Test
    void parseAndApplyDatabaseUrl_withRenderPostgresUrl() {
        String renderUrl = "postgres://drone_admin:secretpass123@dpg-c12345678-a.oregon-postgres.render.com:5432/droneiq_db";
        DroneIqApplication.parseAndApplyDatabaseUrl(renderUrl);

        assertEquals("jdbc:postgresql://dpg-c12345678-a.oregon-postgres.render.com:5432/droneiq_db",
                System.getProperty("spring.datasource.url"));
        assertEquals("drone_admin", System.getProperty("spring.datasource.username"));
        assertEquals("secretpass123", System.getProperty("spring.datasource.password"));
    }

    @Test
    void parseAndApplyDatabaseUrl_withQueryParams() {
        String renderUrl = "postgres://user:pass@dpg-c12345678-a:5432/droneiq_db?sslmode=require";
        DroneIqApplication.parseAndApplyDatabaseUrl(renderUrl);

        assertEquals("jdbc:postgresql://dpg-c12345678-a:5432/droneiq_db?sslmode=require",
                System.getProperty("spring.datasource.url"));
        assertEquals("user", System.getProperty("spring.datasource.username"));
        assertEquals("pass", System.getProperty("spring.datasource.password"));
    }

    @Test
    void parseAndApplyDatabaseUrl_withNullOrBlank_doesNothing() {
        DroneIqApplication.parseAndApplyDatabaseUrl(null);
        assertNull(System.getProperty("spring.datasource.url"));

        DroneIqApplication.parseAndApplyDatabaseUrl("   ");
        assertNull(System.getProperty("spring.datasource.url"));
    }
}
