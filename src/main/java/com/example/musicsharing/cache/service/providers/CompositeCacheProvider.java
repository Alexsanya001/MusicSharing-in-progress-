package com.example.musicsharing.cache.service.providers;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnBean({CaffeineCacheProvider.class, RedisCacheProvider.class})
public class CompositeCacheProvider implements CacheProvider {

    CaffeineCacheProvider caffeineCacheProvider;
    RedisCacheProvider redisCacheProvider;


    @Override
    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference) {
        return caffeineCacheProvider.get(cacheName, key, typeReference).or(
                () -> redisCacheProvider.get(cacheName, key, typeReference)
        );
    }

    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeRef) {
        caffeineCacheProvider.put(cacheName, key, value, ttl, typeRef);
        redisCacheProvider.put(cacheName, key, value, ttl, typeRef);
    }

    @Override
    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference) {
        caffeineCacheProvider.evict(cacheName, key, typeReference);
        redisCacheProvider.evict(cacheName, key, typeReference);
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        caffeineCacheProvider.clear(cacheEntry);
        redisCacheProvider.clear(cacheEntry);
    }
}
