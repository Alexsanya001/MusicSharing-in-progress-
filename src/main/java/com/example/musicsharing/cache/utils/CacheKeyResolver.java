package com.example.musicsharing.cache.utils;

import com.example.musicsharing.cache.annotations.CacheKey;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheKeyResolver {

    private static final Map<Class<?>, Field> cachedKeyFields = new ConcurrentHashMap<>();

    public static Field findKeyField(Class<?> type) {
        return cachedKeyFields.computeIfAbsent(type, cls ->
                Arrays.stream(cls.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(CacheKey.class))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException("No @CacheKey annotation found in " + type)));
    }
}
