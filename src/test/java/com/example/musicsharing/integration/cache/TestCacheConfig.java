package com.example.musicsharing.integration.cache;

import com.example.musicsharing.configs.JacksonConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration
@ComponentScan(basePackages = "com.example.musicsharing.cache")
@Import(JacksonConfig.class)
public class TestCacheConfig {

}
