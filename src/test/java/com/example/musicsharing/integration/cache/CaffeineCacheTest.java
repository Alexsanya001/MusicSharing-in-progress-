package com.example.musicsharing.integration.cache;

import com.example.musicsharing.cache.service.updater.CachedEntityUpdater;
import com.example.musicsharing.cache.service.updater.EntityUpdater;
import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.CacheName;
import com.example.musicsharing.cache.service.CacheService;
import com.example.musicsharing.cache.props.CacheProperties;
import com.example.musicsharing.cache.service.providers.CacheProvider;
import com.example.musicsharing.cache.service.providers.CaffeineCacheProvider;
import com.example.musicsharing.models.entities.TestUser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
    @Autowired
    EntityUpdater updater;

    static final TypeReference<Map<String, List<Long>>> typeRef = new TypeReference<>() {
    };
    static final CacheEntry cacheEntry = CacheEntry.of(CacheName.ALL_USERS, String.class, typeRef);
    @Autowired
    private CachedEntityUpdater cachedEntityUpdater;

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
    void paramTtlOverridesGlobalTtl() {
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

    @Test
    void updateEntity() {
        TestUser user = TestUser.builder()
                .id(10)
                .firstName("John")
                .lastName("Doe")
                .build();

//        TestUser user2 = TestUser.builder()
//                .id(11)
//                .firstName("Peter")
//                .lastName("Parker")
//                .build();
//
//        TestUser user3 = TestUser.builder()
//                .id(12)
//                .firstName("Sam")
//                .lastName("Altman")
//                .build();

//        Set<TestUser> userSet = Set.of(user, user2, user3);
//        Map<String, Set<TestUser>> map = Map.of("first", userSet);

        // cacheService.put(CacheName.ALL_USERS, "key", map, Duration.ofMinutes(1), new TypeReference<>() {});
        cacheService.put(CacheName.LOGGED_USERS, "user", user, Duration.ofMinutes(1), new TypeReference<>() {
        });

        user.setFirstName("Alice");
        user.setLastName("Smith");
//        user2.setFirstName("Bob");
//        user2.setLastName("Marley");
//        user3.setFirstName("Jane");
//        user3.setLastName("Hopkins");
//        updater.updateCachedEntity(user);
//        updater.updateCachedEntity(user2);


//        Map<String, Set<TestUser>> result = cacheService.get(CacheName.ALL_USERS, "key", new TypeReference<Map<String, Set<TestUser>>>() {
//        }).orElse(null);

//        assertNotNull(result);
//        Set<TestUser> resultSet = result.get("first");
//        for (TestUser u : resultSet) {
//            if(u.getId() == 10) {
//                assertEquals("Alice", u.getFirstName());
//                assertEquals("Smith", u.getLastName());
//            }
//            if(u.getId() == 11) {
//                assertEquals("Bob", u.getFirstName());
//                assertEquals("Marley", u.getLastName());
//            }
//            if(u.getId() == 12) {
//                assertEquals("Jane", u.getFirstName());
//                assertEquals("Hopkins", u.getLastName());
//            }
//        }
        TestUser fromCache = cacheService.get(CacheName.LOGGED_USERS, "user", new TypeReference<TestUser>() {
        }).orElse(null);

        assertNotNull(fromCache);
        assertEquals("Alice", fromCache.getFirstName());
    }

    @Test
    void updateEntity_replacesEntityInCache() {
        TestUser originalUser = TestUser.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<TestUser> userSet = new HashSet<>();
        userSet.add(originalUser);
        Map<String, Set<TestUser>> map = Map.of("key", userSet);

        Long start = System.currentTimeMillis();
        cacheService.put(CacheName.ALL_USERS, "cacheKey", map, Duration.ofMinutes(1), new TypeReference<>() {
        });
        Long end = System.currentTimeMillis();
        Duration duration = Duration.ofMillis(end - start);
        log.info("<<<<<<<<<<<< Saved in cache in {} ms >>>>>>>>>>>>>", duration.toMillis());


        TestUser updatedUser = TestUser.builder()
                .id(1)
                .firstName("Alice")
                .lastName("Smith")
                .build();
        Long start1 = System.currentTimeMillis();
        updater.updateCachedEntity(updatedUser);
        Long end1 = System.currentTimeMillis();
        Duration duration1 = Duration.ofMillis(end1 - start1);
        log.info("<<<<<<<<<<<< Updated user in {} ms >>>>>>>>>>>>>", duration1.toMillis());


        Long start2 = System.currentTimeMillis();
        Map<String, Set<TestUser>> result = cacheService.get(CacheName.ALL_USERS, "cacheKey",
                new TypeReference<Map<String, Set<TestUser>>>() {
                }).orElseThrow();
        Long end2 = System.currentTimeMillis();
        Duration duration2 = Duration.ofMillis(end2 - start2);
        log.info("<<<<<<<<<<<< Received from cache in {} ms >>>>>>>>>>>>>", duration2.toMillis());


        Set<TestUser> updatedSet = result.get("key");

        assertEquals(1, updatedSet.size());

        TestUser resultUser = updatedSet.iterator().next();
        assertEquals("Alice", resultUser.getFirstName());
        assertEquals("Smith", resultUser.getLastName());

        assertNotSame(originalUser, resultUser);
/// /////////////////////////////////////////////////////////////////////////////////////////////////
        TestUser originalUser2 = TestUser.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<TestUser> userSet2 = new HashSet<>();
        userSet2.add(originalUser2);
        Map<String, Set<TestUser>> map2 = Map.of("key", userSet2);

        Long start3 = System.currentTimeMillis();
        cacheService.put(CacheName.ALL_USERS, "cacheKey1", map2, Duration.ofMinutes(1), new TypeReference<>() {
        });
        Long end3 = System.currentTimeMillis();
        Duration duration3 = Duration.ofMillis(end3 - start3);
        log.info("<<<<<<<<<<<< Saved in cache in {} ms >>>>>>>>>>>>>", duration3.toMillis());


        TestUser updatedUser2 = TestUser.builder()
                .id(1)
                .firstName("Alice")
                .lastName("Smith")
                .build();
        Long start4 = System.currentTimeMillis();
        updater.updateCachedEntity(updatedUser2);
        Long end4 = System.currentTimeMillis();
        Duration duration4 = Duration.ofMillis(end4 - start4);
        log.info("<<<<<<<<<<<< Updated user in {} ms >>>>>>>>>>>>>", duration4.toMillis());


        Long start5 = System.currentTimeMillis();
        Map<String, Set<TestUser>> result2 = cacheService.get(CacheName.ALL_USERS, "cacheKey1",
                new TypeReference<Map<String, Set<TestUser>>>() {
                }).orElseThrow();
        Long end5 = System.currentTimeMillis();
        Duration duration5 = Duration.ofMillis(end5 - start5);
        log.info("<<<<<<<<<<<< Received from cache in {} ms >>>>>>>>>>>>>", duration5.toMillis());


        Set<TestUser> updatedSet2 = result2.get("key");

        assertEquals(1, updatedSet2.size());

        TestUser resultUser2 = updatedSet2.iterator().next();
        assertEquals("Alice", resultUser2.getFirstName());
        assertEquals("Smith", resultUser2.getLastName());

        assertNotSame(originalUser2, resultUser2);
    }

}
