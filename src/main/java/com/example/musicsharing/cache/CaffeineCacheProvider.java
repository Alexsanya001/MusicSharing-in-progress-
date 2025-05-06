package com.example.musicsharing.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaffeineCacheProvider implements CacheProvider {

    private final Map<Class<?>, Cache<Object, Object>> caches = new ConcurrentHashMap<>();

    @Override
    public <K, V> V get(K key, Class<V> type) {
        return null;
    }

    @Override
    public <K, V> void put(K key, V value) {

    }

    @Override
    public <K> void evict(K key, Class<?> type) {

    }
}
