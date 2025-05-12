package com.example.musicsharing.cache.providers;

import com.example.musicsharing.cache.CacheEntry;
import com.example.musicsharing.cache.CacheName;
import com.example.musicsharing.cache.TypeUtils;
import com.example.musicsharing.cache.props.CacheProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(prefix = "cache.caffeine", name = "enabled", havingValue = "true")
public class CaffeineCacheProvider implements CacheProvider {

    Map<CacheEntry, Cache<Object, TimedValue<?>>> caches = new ConcurrentHashMap<>();
    CacheProperties props;

    @PostConstruct
    private void validateCacheProperties() {
        props.getCaffeine().validate();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference) {
        if (cacheName == null || key == null || typeReference == null) {
            return Optional.empty();
        }
        JavaType valueJavaType = TypeUtils.toJavaType(typeReference);
        CacheEntry cacheEntry = new CacheEntry(cacheName, key.getClass(), valueJavaType);
        return Optional.ofNullable(caches.get(cacheEntry))
                .map(cache -> (TimedValue<V>) cache.getIfPresent(key))
                .map(TimedValue::value);
    }


    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl) {

        if (cacheName == null || key == null || value == null) return;

        CacheEntry cacheEntry = CacheEntry.of(cacheName, key.getClass(), value);
        ttl = (ttl != null ? ttl : props.getCaffeine().getTtl());
        TimedValue<V> timed = new TimedValue<>(value, ttl);
        Cache<Object, TimedValue<?>> cache = getCache(cacheEntry);

        cache.put(key, timed);
    }

    @Override
    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference) {
        if (cacheName != null && key != null && typeReference != null) {
            JavaType valueJavaType = TypeUtils.toJavaType(typeReference);
            CacheEntry cacheEntry = new CacheEntry(cacheName, key.getClass(), valueJavaType);
            Cache<Object, TimedValue<?>> cache = caches.get(cacheEntry);
            if (cache != null) {
                cache.invalidate(key);
            }
        }
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        Cache<?, TimedValue<?>> cache = caches.get(cacheEntry);
        if (cache != null) {
            cache.invalidateAll();
            log.info("Caffeine cache {} : {} has been cleared",
                    cacheEntry.keyType().getSimpleName(), cacheEntry.valueType().getContentType());
        } else if (log.isDebugEnabled()) {
            log.warn("Trying to clear non-existent cache {} :{}",
                    cacheEntry.keyType().getSimpleName(), cacheEntry.valueType().getContentType());
        }
    }

    public <T> void updateCachesWithEntity(T entity) {

    }

    private Cache<Object, TimedValue<?>> getCache(CacheEntry cacheEntry) {
        return caches.computeIfAbsent(cacheEntry, ent ->
                Caffeine.newBuilder()
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
                        .build());
    }

    // Wrapper for value with individual ttl
    private record TimedValue<V>(V value, Duration ttl) {
        public long ttlNanos() {
            return ttl.toNanos();
        }
    }

}

