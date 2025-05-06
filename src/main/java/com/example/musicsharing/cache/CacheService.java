package com.example.musicsharing.cache;

import com.example.musicsharing.cache.providers.CacheProvider;
import com.example.musicsharing.cache.utils.CacheKeyResolver;
import com.example.musicsharing.cache.utils.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheProvider cacheProvider;
    private final RepositoryResolver repositoryResolver;


    @SuppressWarnings("unchecked")
    public <K, V> V get(K key, Class<V> type) {
        V cached = cacheProvider.get(key, type);
        if (cached != null) {
            return cached;
        }

        try {
            V result = (V) getFromRepository(key, type);
            if (result != null) {
                cacheProvider.put(key, result);
            }
            return result;
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new CacheServiceException("Exception thrown by repository method", targetException);
        } catch (IllegalAccessException e) {
            throw new CacheServiceException("Cannot access repository method", e);
        }
    }

    public <K, V> void put(K key, V value) {
        cacheProvider.put(key, value);
    }

    public <K> void evict(K key, Class<?> type) {
        cacheProvider.evict(key, type);
    }


    private <K, V> Object getFromRepository(K key, Class<V> type) throws InvocationTargetException, IllegalAccessException {
        JpaRepository<V, ?> repository = repositoryResolver.findRepository(type);
        Field keyField = CacheKeyResolver.findKeyField(type);
        Method finder = repositoryResolver.findMethod(repository, keyField);
        Object fromRepository = finder.invoke(repository, key);

        if (fromRepository instanceof Optional<?> optional) {
            return optional.orElse(null);
        } else {
            return fromRepository;
        }
    }
}
