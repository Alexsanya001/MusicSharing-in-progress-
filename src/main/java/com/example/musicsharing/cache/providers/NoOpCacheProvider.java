package com.example.musicsharing.cache.providers;

import org.springframework.stereotype.Component;

@Component
public class NoOpCacheProvider implements CacheProvider {

    @Override
    public <K, V> V get(K key, Class<V> type) {
        return null;
    }

    @Override
    public <K, V> void put(K key, V value) {
        // no-op
    }

    @Override
    public <K> void evict(K key, Class<?> type) {
        //no-op
    }
}
