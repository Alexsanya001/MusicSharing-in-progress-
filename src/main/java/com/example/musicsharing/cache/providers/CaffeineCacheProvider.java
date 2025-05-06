package com.example.musicsharing.cache.providers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaffeineCacheProvider implements CacheProvider {

    private final Map<Class<?>, Cache<Object, Object>> caches = new ConcurrentHashMap<>();

    @Override
    public <K, V> V get(K key, Class<V> type) {
        Cache<Object, Object> cache = getCache(type);
        return type.cast(cache.getIfPresent(key));
    }

    @Override
    public <K, V> void put(K key, V value) {
        Cache<Object, Object> cache = getCache(value.getClass());
        cache.put(key, value);
    }

    @Override
    public <K> void evict(K key, Class<?> type) {
        Cache<Object, Object> cache = getCache(type);
        cache.invalidate(key);
    }

    private Cache<Object, Object> getCache(Class<?> type) {
        return caches.computeIfAbsent(type, k ->
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(60))
                        .maximumSize(100000)
                        .build());
    }
}
