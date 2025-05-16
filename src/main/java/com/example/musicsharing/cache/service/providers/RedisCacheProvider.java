package com.example.musicsharing.cache.service.providers;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.exceptions.CacheException;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.util.TypeUtils;
import com.example.musicsharing.cache.props.CacheProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cache.redis", name = "enabled", havingValue = "true")
public class RedisCacheProvider implements CacheProvider {

    Map<JavaType, RedisTemplate<String, ?>> templates = new ConcurrentHashMap<>();
    CacheProperties props;
    RedisConnectionFactory factory;
    ObjectMapper objectMapper;

    @PostConstruct
    private void validateCacheProperties() {
        CacheProperties.validate(props.getRedis());
    }


    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Optional<V> get(CacheName cacheName, K key, TypeReference<V> typeRef) {
        String redisKey = generateKey(cacheName, key, typeRef);
        return (Optional<V>) getTemplate(TypeUtils.toJavaType(typeRef))
                .map(template -> template.opsForValue().get(redisKey));
    }


    @Override
    public <K, V> void put(CacheName cacheName, K key, V value, Duration ttl, TypeReference<V> typeRef) {
        RedisTemplate<String, V> template = computeIfAbsent(TypeUtils.toJavaType(typeRef));
        String redisKey = generateKey(cacheName, key, typeRef);
        ttl = (ttl == null ? props.getRedis().getTtl() : ttl);
        template.opsForValue().set(redisKey, value, ttl);
    }

    @Override
    public <K, V> void evict(CacheName cacheName, K key, TypeReference<V> typeRef) {
        String redisKey = generateKey(cacheName, key, typeRef);
        getTemplate(TypeUtils.toJavaType(typeRef))
                .ifPresent(template -> template.delete(redisKey));
    }


    @Override
    public void clear(CacheEntry cacheEntry) {
        getTemplate(cacheEntry.valueJavaType())
                .ifPresent(template -> {
                    String prefix = cacheEntry.cacheName().name() + ":";
                    Set<String> keys = template.keys(prefix + "*");
                    if (!keys.isEmpty()) {
                        template.delete(keys);
                        log.info("Cleared {} Redis keys for cache {}", keys.size(), cacheEntry.cacheName());
                    }
                });

    }


    @SuppressWarnings("unchecked")
    private <V> RedisTemplate<String, V> computeIfAbsent(JavaType javaType) {
        return (RedisTemplate<String, V>) templates.computeIfAbsent(javaType, k -> {
            RedisTemplate<String, V> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            template.setKeySerializer(RedisSerializer.string());
            RedisSerializer<V> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, javaType);
            template.setValueSerializer(valueSerializer);
            template.afterPropertiesSet();
            return template;
        });
    }


     private Optional<RedisTemplate<String, ?>> getTemplate(JavaType javaType) {
        return Optional.ofNullable(templates.get(javaType));
    }

    private String generateKey(CacheName cacheName, Object key, TypeReference<?> typeRef) {
        try {
            JavaType javaType = TypeUtils.toJavaType(typeRef);
            String valueTypeShort = javaType.getRawClass().getName();
            String keyTypeShort = key.getClass().getName();
            String serializedKey = objectMapper.writeValueAsString(key);
            String hashedKey = sha256Hex(serializedKey);

            return String.join(":", cacheName.name(), valueTypeShort, keyTypeShort, hashedKey);

        } catch (JsonProcessingException e) {
            throw new CacheException("Failed to serialize key " + key, e);
        }

    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new CacheException("SHA-256 algorithm not available", e);
        }
    }

}
