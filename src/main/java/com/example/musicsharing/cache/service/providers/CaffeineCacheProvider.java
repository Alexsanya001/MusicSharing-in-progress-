package com.example.musicsharing.cache.service.providers;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.service.CacheRegistry;
import com.example.musicsharing.cache.wrappers.TimedValue;
import com.example.musicsharing.cache.props.CacheProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(prefix = "cache.caffeine", name = "enabled", havingValue = "true")
public class CaffeineCacheProvider implements CacheProvider {


    CacheProperties props;
    CacheRegistry cacheRegistry;

    @PostConstruct
    private void validateCacheProperties() {
        CacheProperties.validate(props.getCaffeine());
    }


    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeReference) {
        if (cacheName == null || key == null || typeReference == null) {
            return Optional.empty();
        }
        CacheEntry cacheEntry = CacheEntry.of(cacheName, key.getClass(), typeReference);
        return (Optional<V>) cacheRegistry.getCache(cacheEntry)
                .map(cache -> cache.getIfPresent(key))
                .map(TimedValue::value);
    }


    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeRef) {

        if (cacheName == null || key == null || value == null) return;

        CacheEntry cacheEntry = CacheEntry.of(cacheName, key.getClass(), typeRef);
        ttl = (ttl != null ? ttl : props.getCaffeine().getTtl());
        TimedValue<V> timed = new TimedValue<>(value, ttl);
        Cache<Object, TimedValue<?>> cache = cacheRegistry.computeIfAbsent(cacheEntry);

        cache.put(key, timed);
    }


    @Override
    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeReference) {
        if (cacheName != null && key != null && typeReference != null) {
            CacheEntry cacheEntry = CacheEntry.of(cacheName, key.getClass(), typeReference);
            cacheRegistry.getCache(cacheEntry).ifPresent(c -> c.invalidate(key));
        }
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        cacheRegistry.getCache(cacheEntry).ifPresent(Cache::invalidateAll);
        log.info("Caffeine cache {} : {} has been cleared",
                cacheEntry.keyType().getSimpleName(), cacheEntry.valueJavaType().getContentType());
    }


    public <T> void updateCachesWithEntity(T entity) {
    }
}

