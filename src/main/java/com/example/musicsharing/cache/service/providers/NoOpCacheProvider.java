package com.example.musicsharing.cache.service.providers;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnMissingBean({CaffeineCacheProvider.class, RedisCacheProvider.class})
public class NoOpCacheProvider implements CacheProvider {

    @Override
    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference) {
        return Optional.empty();
    }

    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeRef) {
        // no-op
    }

    @Override
    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference) {
        //no-op
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        //no-op
    }
}
