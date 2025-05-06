package com.example.musicsharing.cache;

import java.lang.reflect.Field;
import java.util.Arrays;

public class CacheKeyResolver {

    public static Field findKeyField(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CacheKey.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No @CacheKey annotation found in " + type));
    }
}
