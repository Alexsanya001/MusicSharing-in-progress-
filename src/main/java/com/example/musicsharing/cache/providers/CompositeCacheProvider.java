package com.example.musicsharing.cache.providers;

import com.example.musicsharing.cache.CacheEntry;
import com.example.musicsharing.cache.CacheName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnBean({CaffeineCacheProvider.class, RedisCacheProvider.class})
public class CompositeCacheProvider implements CacheProvider {

    CaffeineCacheProvider caffeineCacheProvider;
    RedisCacheProvider redisCacheProvider;


    @Override
    public <K, V> V get(CacheName cacheName, K key, Class<V> type) {
        V value = caffeineCacheProvider.get(cacheName, key, type);
        if (value != null) {
            return value;
        }
        return redisCacheProvider.get(cacheName, key, type);
    }

    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl) {
        caffeineCacheProvider.put(cacheName, key, value, ttl);
        redisCacheProvider.put(cacheName, key, value, ttl);
    }

    @Override
    public <K> void evict(CacheName cacheName, K key, Class<?> type) {
        caffeineCacheProvider.evict(cacheName, key, type);
        redisCacheProvider.evict(cacheName, key, type);
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        caffeineCacheProvider.clear(cacheEntry);
        redisCacheProvider.clear(cacheEntry);
    }
}
