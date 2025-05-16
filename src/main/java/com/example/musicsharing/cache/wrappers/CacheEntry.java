package com.example.musicsharing.cache.wrappers;

import com.example.musicsharing.cache.util.TypeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public record CacheEntry(CacheName cacheName, Class<?> keyType, JavaType valueJavaType) {

    public static CacheEntry of(CacheName cacheName, Class<?> keyType, TypeReference<?> ref) {
        return new CacheEntry(cacheName, keyType, TypeUtils.toJavaType(ref));
    }
}
