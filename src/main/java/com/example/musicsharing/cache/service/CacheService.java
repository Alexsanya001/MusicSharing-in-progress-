package com.example.musicsharing.cache.service;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.service.providers.CacheProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheProvider cacheProvider;


    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference) {
        return cacheProvider.get(cacheName, key, typeReference);
    }

    @Async
    public <K, V> void put(CacheName cacheName, K key, V value, TypeReference<V> typeRef) {
        cacheProvider.put(cacheName, key, value, null, typeRef);
    }

    @Async
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeRef) {
        cacheProvider.put(cacheName, key, value, ttl, typeRef);
    }

    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference) {
        cacheProvider.evict(cacheName, key, typeReference);
    }

    public void clear(CacheEntry cacheEntry) {
        cacheProvider.clear(cacheEntry);
    }
}
