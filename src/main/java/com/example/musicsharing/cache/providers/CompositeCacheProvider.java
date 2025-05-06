package com.example.musicsharing.cache.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompositeCacheProvider implements CacheProvider {

    private final CaffeineCacheProvider caffeineCacheProvider;
    private final RedisCacheProvider redisCacheProvider;


    @Override
    public <K, V> V get(K key, Class<V> type) {
        V value = caffeineCacheProvider.get(key, type);
        if (value != null) {
            return value;
        } else {
            return redisCacheProvider.get(key, type);
        }
    }

    @Override
    public <K, V> void put(K key, V value) {
        caffeineCacheProvider.put(key, value);
        redisCacheProvider.put(key, value);
    }

    @Override
    public <K> void evict(K key, Class<?> type) {
        caffeineCacheProvider.evict(key, type);
        redisCacheProvider.evict(key, type);
    }
}
