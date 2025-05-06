package com.example.musicsharing.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheProvider cacheProvider;
    private final RepositoryResolver repositoryResolver;

    @SuppressWarnings("unchecked")
    public <K, V> V get(K key, Class<V> type) throws InvocationTargetException, IllegalAccessException {
        V cached = cacheProvider.get(key, type);
        if (cached != null) {
            return cached;
        }
        JpaRepository<V, ?> repository = repositoryResolver.findRepository(type);
        Field keyField = CacheKeyResolver.findKeyField(type);
        Method finder = repositoryResolver.findMethod(repository, keyField, key);
        return (V) finder.invoke(repository, key);
    }

    public <K, V> void put(K key, V value) {
        cacheProvider.put(key, value);
    }

    public <K> void evict(K key, Class<?> type) {
        cacheProvider.evict(key, type);
    }
}
