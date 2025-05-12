package com.example.musicsharing.cache.providers;

import com.example.musicsharing.cache.CacheEntry;
import com.example.musicsharing.cache.CacheName;
import com.example.musicsharing.cache.props.CacheProperties;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cache.redis", name = "enabled", havingValue = "true")
public class RedisCacheProvider implements CacheProvider {

    Map<CacheEntry, RedisTemplate<String, Object>> templates = new ConcurrentHashMap<>();
    CacheProperties props;
    RedisConnectionFactory factory;

    @PostConstruct
    private void validateCacheProperties() {
        props.getRedis().validate();
    }


    @Override
    public <K, V> Optional<V> get(CacheName cacheName, K key, Class<V> type) {
        String redisKey = cacheName.name() + ":" + key.toString() + ":" + type.getSimpleName();
        RedisTemplate<String, Object> template = getTemplate(cacheName, key.getClass(), type);
        return Optional.ofNullable(type.cast(template.opsForValue().get(redisKey)));
    }

    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl) {
        String redisKey = cacheName.name() + ":" + key.toString() + ":" + value.getClass().getSimpleName();
        RedisTemplate<String, Object> template = getTemplate(cacheName, key.getClass(), value.getClass());
        ttl = (ttl == null ? props.getRedis().getTtl() : ttl);
        template.opsForValue().set(redisKey, value, ttl);
    }

    @Override
    public <K> void evict(CacheName cacheName, K key, Class<?> type) {
        String redisKey = cacheName.name() + ":" + key.toString() + ":" + type.getSimpleName();
        RedisTemplate<String, Object> template = getTemplate(cacheName, key.getClass(), type);
        template.delete(redisKey);
    }

    @Override
    public void clear(CacheEntry cacheEntry) {
        //@TODO To think about SCAN
        log.warn("REDIS CLEAR BY CACHE_ENTRY NOT REALIZED YET");
    }

    private RedisTemplate<String, Object> getTemplate(CacheName cacheName, Class<?> keyType, Class<?> valueType) {
        CacheEntry cacheEntry = new CacheEntry(cacheName, keyType, valueType);
        return templates.computeIfAbsent(cacheEntry, k -> {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            template.setKeySerializer(new GenericToStringSerializer<>(keyType));
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.afterPropertiesSet();
            return template;
        });
    }

}
