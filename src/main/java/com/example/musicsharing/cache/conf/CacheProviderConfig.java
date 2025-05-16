package com.example.musicsharing.cache.conf;

import com.example.musicsharing.cache.props.CacheProperties;
import com.example.musicsharing.cache.service.providers.CacheProvider;
import com.example.musicsharing.cache.service.providers.CaffeineCacheProvider;
import com.example.musicsharing.cache.service.providers.CompositeCacheProvider;
import com.example.musicsharing.cache.service.providers.NoOpCacheProvider;
import com.example.musicsharing.cache.service.providers.RedisCacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheProviderConfig {

    @Bean
    CacheProvider cacheProvider(
            ObjectProvider<CompositeCacheProvider> composite,
            ObjectProvider<CaffeineCacheProvider> caffeine,
            ObjectProvider<RedisCacheProvider> redis,
            ObjectProvider<NoOpCacheProvider> noOp) {

        CacheProvider provider = composite.getIfAvailable();
        if (provider != null) {
            return provider;
        }
        provider = caffeine.getIfAvailable();
        if (provider != null) {
            return provider;
        }
        provider = redis.getIfAvailable();
        if (provider != null) {
            return provider;
        }
        return fallBack(noOp);
    }

    private CacheProvider fallBack(ObjectProvider<NoOpCacheProvider> noOp) {
        log.info("No cache provider configured. Falling back to no-op cache.");
        return noOp.getIfAvailable();
    }
}
