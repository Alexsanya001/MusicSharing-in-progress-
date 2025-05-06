package com.example.musicsharing.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheProviderConfig {

    @Bean
    CacheProvider cacheProvider(CacheProperties props,
                                RedisCacheProvider redis,
                                CaffeineCacheProvider caffeine,
                                CompositeCacheProvider composite,
                                NoOpCacheProvider noOp) {
        return switch (props.getType().toLowerCase()) {
            case "redis" -> redis;
            case "caffeine" -> caffeine;
            case "both" -> composite;
            default -> noOp;
        };
    }
}
