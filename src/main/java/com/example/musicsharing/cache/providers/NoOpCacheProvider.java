package com.example.musicsharing.cache.providers;

import com.example.musicsharing.cache.CacheEntry;
import com.example.musicsharing.cache.CacheName;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnMissingBean({CaffeineCacheProvider.class, RedisCacheProvider.class})
public class NoOpCacheProvider implements CacheProvider {

    @Override
    public <K, V> V get(CacheName cacheName, K key, Class<V> type) {
        return null;
    }

    @Override
    public <K, V> void put(CacheName cacheName,K key, V value, Duration ttl) {
        // no-op
    }

    @Override
    public <K> void evict(CacheName cacheName,K key, Class<?> type) {
        //no-op
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        //no-op
    }
}
