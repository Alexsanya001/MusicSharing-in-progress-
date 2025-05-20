package com.example.musicsharing.cache.service.updater;

import com.example.musicsharing.cache.service.CacheRegistry;
import com.example.musicsharing.cache.wrappers.CacheEntry;
import com.example.musicsharing.cache.wrappers.TimedValue;
import com.fasterxml.jackson.databind.JavaType;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityUpdater {

    private final EntityIndex entityIndex;
    private final CacheRegistry cacheRegistry;
    private final CachedEntityUpdater cachedEntityUpdater;
    private final TypeSearchPathBuilder pathBuilder = new TypeSearchPathBuilder();




    public void updateCachedEntity(Object toUpdate) {
        Map<CacheEntry, Map<EntityId, Set<Object>>> index = entityIndex.getIndex();
        Object id = entityIndex.extractId(toUpdate);
        EntityId idAndClassToUpdate = new EntityId(toUpdate.getClass(), id);

        for (var indexEntry : index.entrySet()) {

            for (var entityId : indexEntry.getValue().keySet()) {

                if (entityId.equals(idAndClassToUpdate)) {
                    CacheEntry cacheEntry = indexEntry.getKey();
                    updateInValues(cacheEntry, toUpdate, id, indexEntry.getValue().get(entityId));
                }
            }
        }
    }

    private void updateInValues(CacheEntry cacheEntry, Object toUpdate, Object id, Set<Object> cacheKeys) {
        JavaType javaType = cacheEntry.valueJavaType();
        Class<?> targetClass = toUpdate.getClass();
        Cache<Object, TimedValue<?>> cache = cacheRegistry.getCache(cacheEntry).orElse(null);

        List<SearchStep> path = pathBuilder.buildSearchPath(javaType, targetClass);

        for (Object cacheKey : cacheKeys) {
            if (cache != null) {
                TimedValue<?> cachedTimed = cache.getIfPresent(cacheKey);
                if (cachedTimed != null) {
                    Object cachedValue = cachedTimed.value();
                    if (cachedValue != null) {
                        Object updatedValue = cachedEntityUpdater.updateCachedValue(cachedValue, path, toUpdate, id);
                        TimedValue<?> timed = new TimedValue<>(updatedValue, cachedTimed.ttl());
                        cache.put(cacheKey, timed);
                    }
                }
            }
        }
    }


}
