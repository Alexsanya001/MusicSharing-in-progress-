package com.example.musicsharing.integration.cache;

import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.service.CacheService;
import com.example.musicsharing.cache.props.CacheProperties;
import com.example.musicsharing.cache.service.providers.CacheProvider;
import com.example.musicsharing.cache.service.providers.CaffeineCacheProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestCacheConfig.class})
@TestPropertySource(properties = {
        "cache.redis.enabled=false",
        "cache.caffeine.enabled=true",
        "cache.caffeine.ttl=1s",
        "cache.caffeine.max-size=1000"
})
@Log4j2
public class CaffeineCacheTest {

    @Autowired
    CacheService cacheService;
    @Autowired
    CacheProvider cacheProvider;
    @Autowired
    CacheProperties props;

    static final TypeReference<Map<String, List<Long>>> typeRef = new TypeReference<>() {
    };
    static final CacheEntry cacheEntry = CacheEntry.of(CacheName.ALL_USERS, String.class, typeRef);

    @BeforeEach
    public void init() {
        cacheService.clear(cacheEntry);
    }


    @Test
    void contextLoadsCorrectly() {
        assertNotNull(cacheService);
        assertNotNull(cacheProvider);
        assertInstanceOf(CaffeineCacheProvider.class, cacheProvider);
    }

    @Test
    void getReturnsCached() {
        Map<String, List<Long>> expected = Map.of("first", List.of(1L, 2L, 3L), "second", List.of(4L, 5L, 6L));
        cacheService.put(CacheName.ALL_USERS, "myCache", expected, new TypeReference<>() {
        });

        Map<?, ?> result = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void anotherTypesReturnsNull() {
        Map<String, List<Long>> map = Map.of("first", List.of(1L, 2L, 3L), "second", List.of(4L, 5L, 6L));
        cacheService.put(CacheName.ALL_USERS, "myCache", map, new TypeReference<>() {
        });

        Map<?, ?> anotherKey = cacheService.get(CacheName.ALL_USERS, "WRONG_KEY", typeRef).orElse(null);

        Map<?, ?> anotherCacheName = cacheService.get(CacheName.MUSIC_FILES, "myCache", typeRef).orElse(null);

        Map<?, ?> anotherType = cacheService.get(CacheName.ALL_USERS, "myCache", new TypeReference<Map<Long, List<String>>>() {
        }).orElse(null);

        assertNull(anotherKey);
        assertNull(anotherType);
        assertNull(anotherCacheName);

        assertNotNull(cacheService.get(CacheName.ALL_USERS, "myCache", typeRef)
                .orElse(null));
    }

    @Test
    void afterEvictionReturnsNull() {
        Map<String, List<Long>> map = Map.of("first", List.of(1L, 2L, 3L), "second", List.of(4L, 5L, 6L));
        cacheService.put(CacheName.ALL_USERS, "myCache", map, typeRef);
        var beforeEviction = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

        cacheService.evict(CacheName.ALL_USERS, "myCache", typeRef);
        var afterEviction = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

        assertNull(afterEviction);
        assertNotNull(beforeEviction);
    }

    @Test
    void returnsNullAfterCacheExpired() {
        Map<String, List<Long>> map = Map.of("first", List.of(1L, 2L, 3L), "second", List.of(4L, 5L, 6L));
        cacheService.put(CacheName.ALL_USERS, "myCache", map, typeRef);

        var result = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

        assertNotNull(result);
        assertEquals(map, result);

        try {
            Thread.sleep(props.getCaffeine().getTtl().toMillis());
            var result2 = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

            assertNull(result2);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void paramTtlOverridesGlobalTtl(){
        Map<String, List<Long>> map = Map.of("first", List.of(1L, 2L, 3L), "second", List.of(4L, 5L, 6L));
        Duration paramTtl = Duration.ofSeconds(2);
        cacheService.put(CacheName.ALL_USERS, "myCache", map, paramTtl, typeRef);

        try {
            Thread.sleep(props.getCaffeine().getTtl().toMillis());
            var result = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

            assertNotNull(result);
            assertEquals(map, result);

            Thread.sleep(paramTtl.toMillis());
            var result2 = cacheService.get(CacheName.ALL_USERS, "myCache", typeRef).orElse(null);

            assertNull(result2);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
