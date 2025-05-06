package com.example.musicsharing.cache.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private String type;
    private Duration ttl;
    private int maxSize;
}
