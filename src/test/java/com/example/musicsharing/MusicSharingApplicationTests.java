package com.example.musicsharing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;


@SpringBootTest
@ActiveProfiles("test")
class MusicSharingApplicationTests {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("test-db")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    static void setUp() {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @Test
    void contextLoads() {
    }

    @AfterAll
    public static void tearDown() {
        postgres.stop();
    }
}
