package com.example.musicsharing.cache;

import com.example.musicsharing.cache.providers.CacheProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheProvider cacheProvider;


    public <K, V> V get(CacheName cacheName, K key, Class<V> type) {
        return cacheProvider.get(cacheName, key, type);
    }

    public <K, V> void put(CacheName cacheName, K key, V value) {
        cacheProvider.put(cacheName, key, value, null);
    }

    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl) {
        cacheProvider.put(cacheName, key, value, ttl);
    }

    public <K> void evict(CacheName cacheName, K key, Class<?> type) {
        cacheProvider.evict(cacheName,key, type);
    }
}
