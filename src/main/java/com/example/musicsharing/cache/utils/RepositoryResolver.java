package com.example.musicsharing.cache.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RepositoryResolver {
    private final ApplicationContext context;

    Map<Class<?>, JpaRepository<?, ?>> cachedRepos = new ConcurrentHashMap<>();
    Map<MethodCacheKey, Method> cachedMethods = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> JpaRepository<T, ?> findRepository(Class<T> entityType) {
        return (JpaRepository<T, ?>) cachedRepos.computeIfAbsent(entityType, type ->
                context.getBeansOfType(JpaRepository.class).values().stream()
                        .filter(repo -> Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(
                                repo.getClass(), JpaRepository.class))[0].equals(entityType))
                        .map(r -> (JpaRepository<T, ?>) r)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No repository found for " + entityType))
        );
    }

    public Method findMethod(JpaRepository<?, ?> repository, Field keyField) {
        MethodCacheKey cacheKey = new MethodCacheKey(repository.getClass(), keyField.getName());
        return cachedMethods.computeIfAbsent(cacheKey, key -> {
            String methodName = "findBy" + StringUtils.capitalize(key.fieldName());
            return Arrays.stream(key.repositoryClass().getMethods())
                    .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("No method %s found in repository %s", methodName, key.repositoryClass().getName())));
        });
    }

    private record MethodCacheKey(Class<?> repositoryClass, String fieldName) {
    }
}
