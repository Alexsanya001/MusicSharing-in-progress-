package com.example.musicsharing.cache.service;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.wrappers.TimedValue;
import com.example.musicsharing.cache.props.CacheProperties;
import com.example.musicsharing.cache.service.providers.CaffeineCacheProvider;
import com.fasterxml.jackson.databind.JavaType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnBean(CaffeineCacheProvider.class)
@RequiredArgsConstructor
public class CacheRegistry {

    @Getter(AccessLevel.PACKAGE)
    CacheProperties props;

    @Getter
    Map<CacheName, Map<Class<?>, List<Entry>>> registry = new ConcurrentHashMap<>();

    private record Entry(JavaType valueJavaType, Cache<Object, TimedValue<?>> cache) {
    }

    public Optional<Cache<Object, TimedValue<?>>> getCache(CacheEntry requestedEntry) {
        Map<Class<?>, List<Entry>> keyTypeMap = registry.get(requestedEntry.cacheName());
        if (keyTypeMap == null) return Optional.empty();

        List<Entry> entries = keyTypeMap.get(requestedEntry.keyType());
        if (entries == null) return Optional.empty();

        return getExistingCache(entries, requestedEntry);
    }


    public Cache<Object, TimedValue<?>> computeIfAbsent(CacheEntry cacheEntry) {
        synchronized (this) {
            Map<Class<?>, List<Entry>> keyTypeMap = registry.computeIfAbsent(cacheEntry.cacheName(),
                    k -> new ConcurrentHashMap<>());

            List<Entry> entries = keyTypeMap.computeIfAbsent(cacheEntry.keyType(),
                    k -> new CopyOnWriteArrayList<>());


            return getExistingCache(entries, cacheEntry).orElseGet(() -> {
                Cache<Object, TimedValue<?>> newCache = createCache();
                entries.add(new Entry(cacheEntry.valueJavaType(), newCache));
                return newCache;
            });
        }
    }

    private Cache<Object, TimedValue<?>> createCache() {

        return Caffeine.newBuilder()
                .expireAfter(new Expiry<Object, TimedValue<?>>() {
                    @Override
                    public long expireAfterCreate(@Nonnull Object key, @Nonnull TimedValue<?> value, long currentTime) {
                        return value.ttlNanos();
                    }

                    @Override
                    public long expireAfterUpdate(@Nonnull Object key, @Nonnull TimedValue<?> value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@Nonnull Object key, @Nonnull TimedValue<?> value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .maximumSize(props.getCaffeine().getMaxSize())
                .build();
    }

    private Optional<Cache<Object, TimedValue<?>>> getExistingCache(List<Entry> entries, CacheEntry cacheEntry) {
        for (Entry existing : entries) {
            if (cacheEntry.valueJavaType().equals(existing.valueJavaType())) {
                return Optional.of(existing.cache());
            }
        }
        return Optional.empty();
    }
}
