package com.example.musicsharing.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public record CacheEntry(CacheName cacheName, Class<?> keyType, JavaType valueType) {

    public static CacheEntry of(CacheName cacheName, Class<?> keyType, Object value) {
        return new CacheEntry(cacheName, keyType, TypeUtils.toJavaType(value));
    }

    public static CacheEntry of(CacheName cacheName, Class<?> keyType, TypeReference<?> ref) {
        return new CacheEntry(cacheName, keyType, TypeUtils.toJavaType(ref));
    }
}
