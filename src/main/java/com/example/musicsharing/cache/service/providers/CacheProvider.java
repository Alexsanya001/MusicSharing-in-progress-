package com.example.musicsharing.cache.service.providers;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Optional;

public interface CacheProvider {

    <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference);

    <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeReference);

    <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference);

    void clear(CacheEntry cacheEntry);
}
