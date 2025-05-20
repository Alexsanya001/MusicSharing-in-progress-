package com.example.musicsharing.cache.service.updater;

import com.example.musicsharing.cache.annotations.Tracked;
import com.example.musicsharing.cache.exceptions.CacheException;
import com.example.musicsharing.cache.wrappers.CacheEntry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class EntityIndex {


    private final Map<CacheEntry, Map<EntityId, Set<Object>>> index = new ConcurrentHashMap<>();

    public void register(CacheEntry cacheEntry, Object key, Object value) {
        Set<EntityRef> entityRefs = new HashSet<>();

        entityRefs.addAll(scanForTrackedEntities(key, key));
        entityRefs.addAll(scanForTrackedEntities(value, key));

        for (EntityRef ref : entityRefs) {
            index
                    .computeIfAbsent(cacheEntry, __ -> new ConcurrentHashMap<>())
                    .computeIfAbsent(ref.entityId(), __ -> ConcurrentHashMap.newKeySet())
                    .add(ref.cacheKey());
        }
    }

    private Set<EntityRef> scanForTrackedEntities(Object candidate, Object cacheKey) {
        Set<EntityRef> refs = new HashSet<>();

        if (candidate == null) return refs;

        if (isTracked(candidate)) {
            Object id = extractId(candidate);
            if (id != null) {
                refs.add(new EntityRef(new EntityId(candidate.getClass(), id), cacheKey));
            }
        } else if (candidate instanceof Collection<?> collection) {
            for (Object item : collection) {
                refs.addAll(scanForTrackedEntities(item, cacheKey));
            }
        } else if (candidate instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                refs.addAll(scanForTrackedEntities(entry.getKey(), cacheKey));
                refs.addAll(scanForTrackedEntities(entry.getValue(), cacheKey));
            }
        }
        return refs;
    }

    private boolean isTracked(Object entity) {
        return entity != null && entity.getClass().isAnnotationPresent(Tracked.class);
    }

    protected Optional<Object> extractId(Object entity) {
        if (entity == null) {
            return Optional.empty();
        }
        Tracked tracked = entity.getClass().getAnnotation(Tracked.class);
        if (tracked == null) {
            return Optional.empty();
        }

        String idField = tracked.idField();
        try {
            Field field = entity.getClass().getDeclaredField(idField);
            field.setAccessible(true);
            return Optional.ofNullable(field.get(entity));
        } catch (NoSuchFieldException e) {
            throw new CacheException("Not found id-field %s in class %s".formatted(idField, entity.getClass()), e);
        } catch (SecurityException | IllegalAccessException e) {
            throw new CacheException("Can't access id-field %s in class %s".formatted(idField, entity.getClass()), e);
        }
    }

    private record EntityRef(EntityId entityId, Object cacheKey) {
    }

}

record EntityId(Class<?> type, Object id) {
}
