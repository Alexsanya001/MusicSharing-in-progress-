package com.example.musicsharing.cache;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private String type;
    private Duration ttl;
    private int maxSize;
}
