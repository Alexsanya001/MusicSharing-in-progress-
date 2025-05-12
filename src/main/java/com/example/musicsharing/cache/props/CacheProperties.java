package com.example.musicsharing.cache.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private CaffeineProperties caffeine = new CaffeineProperties();
    private RedisProperties redis = new RedisProperties();

    @Getter
    @Setter
    public static class CaffeineProperties {
        private boolean enabled;
        private Duration ttl;
        private Long maxSize;

        public void validate() {
            if (enabled) {
                if (ttl == null) {
                    throw new IllegalStateException("Missing required property 'cache.caffeine.ttl'");
                }
                if (ttl.isNegative() || ttl.isZero()) {
                    throw new IllegalArgumentException("Property cache.caffeine.ttl must be positive");
                }
                if (maxSize == null) {
                    throw new IllegalStateException("Missing required property 'cache.caffeine.max-size'");
                }
                if(maxSize <= 0) {
                    throw new IllegalArgumentException("Property cache.caffeine.max-size must be positive");
                }
            }
        }
    }

    @Getter
    @Setter
    public static class RedisProperties {
        private boolean enabled;
        private Duration ttl;

        public void validate() {
            if (enabled) {
                if (ttl == null) {
                    throw new IllegalStateException("Missing required property 'cache.caffeine.ttl'");
                }
                if (ttl.isNegative() || ttl.isZero()) {
                    throw new IllegalArgumentException("Property cache.caffeine.ttl must be positive");
                }
            }
        }
    }
}