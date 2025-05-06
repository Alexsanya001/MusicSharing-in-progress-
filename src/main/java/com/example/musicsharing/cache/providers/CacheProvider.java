package com.example.musicsharing.cache.providers;

public interface CacheProvider {

    <K, V> V get(K key, Class<V> type);

    <K, V> void put(K key, V value);

    <K> void evict(K key, Class<?> type);
}
