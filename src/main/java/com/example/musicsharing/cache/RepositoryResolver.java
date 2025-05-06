package com.example.musicsharing.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RepositoryResolver {
    private final ApplicationContext context;

    @SuppressWarnings("unchecked")
    public <T> JpaRepository<T, ?> findRepository(Class<T> entityType) {
        return context.getBeansOfType(JpaRepository.class).values().stream()
                .filter(repo -> Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(
                        repo.getClass(), JpaRepository.class))[0].equals(entityType))
                .map(r -> (JpaRepository<T, ?>) r)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No repository found for " + entityType));
    }

    public Method findMethod(JpaRepository<?,?> repository, Field keyField, Object value) {
        String methodName = "findBy" + StringUtils.capitalize(keyField.getName());
        return Arrays.stream(repository.getClass().getMethods())
                .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No such method: " + methodName));
    }
}
