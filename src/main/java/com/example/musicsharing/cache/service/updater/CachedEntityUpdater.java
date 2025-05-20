package com.example.musicsharing.cache.service.updater;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CachedEntityUpdater {
    private final EntityIndex entityIndex;

    public Object updateCachedValue(Object cachedValue, List<SearchStep> path, Object toUpdate, Object targetId) {
        return updateRecursively(cachedValue, path, 0, toUpdate, targetId);
    }

    private Object updateRecursively(Object current, List<SearchStep> path, int index, Object toUpdate, Object targetId) {
        if (index >= path.size()) return current;

        SearchStep step = path.get(index);

        if (step instanceof DirectHit) {
            Object currentId = entityIndex.extractId(current);
            return Objects.equals(currentId, targetId) ? toUpdate : current;
        }

        if (step instanceof MapKey && current instanceof Map<?, ?> map) {
            Map<Object, Object> updated = new LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                Object key = updateRecursively(entry.getKey(), path, index + 1, toUpdate, targetId);
                updated.put(key, entry.getValue());
            }
            return updated;
        }

        if (step instanceof MapValue && current instanceof Map<?, ?> map) {
            Map<Object, Object> updated = new LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                Object value = updateRecursively(entry.getValue(), path, index + 1, toUpdate, targetId);
                updated.put(entry.getKey(), value);
            }
            return updated;
        }

        if (step instanceof CollectionElement && current instanceof Collection<?> collection) {
            Collection<Object> updated =
                    (current instanceof List<?>) ? new ArrayList<>()
                            : (current instanceof Set<?>) ? new LinkedHashSet<>()
                            : new ArrayList<>();

            for (var element : collection) {
                Object updatedElement = updateRecursively(element, path, index + 1, toUpdate, targetId);
                updated.add(updatedElement);
            }
            return updated;
        }
        return current;
    }
}
