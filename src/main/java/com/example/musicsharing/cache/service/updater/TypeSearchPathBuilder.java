package com.example.musicsharing.cache.service.updater;

import com.fasterxml.jackson.databind.JavaType;

import java.util.ArrayList;
import java.util.List;

public class TypeSearchPathBuilder {

    public List<SearchStep> buildSearchPath(JavaType javaType, Class<?> targetClass) {
        return build(javaType, targetClass, new ArrayList<>());
    }

    private List<SearchStep> build(JavaType current, Class<?> targetClass, List<SearchStep> steps) {
        if (current.getRawClass().equals(targetClass)) {
            steps.add(new DirectHit());
            return steps;
        }

        if (current.isMapLikeType()) {
            JavaType keyType = current.containedTypeOrUnknown(0);
            JavaType valueType = current.containedTypeOrUnknown(1);

            if (isOrContains(keyType, targetClass)) {
                steps.add(new MapKey());
                return build(keyType, targetClass, steps);
            } else if (isOrContains(valueType, targetClass)) {
                steps.add(new MapValue());
                return build(valueType, targetClass, steps);
            }
        }

        if (current.isCollectionLikeType()) {
            JavaType elementType = current.containedTypeOrUnknown(0);
            if (isOrContains(elementType, targetClass)) {
                steps.add(new CollectionElement());
                return build(elementType, targetClass, steps);
            }
        }

        for (JavaType bound : current.getBindings().getTypeParameters()) {
            if (isOrContains(bound, targetClass)) {
                return build(bound, targetClass, steps);
            }
        }

        return List.of();
    }

    private boolean isOrContains(JavaType container, Class<?> target) {
        if (container.getRawClass().equals(target)) return true;

        if (container.isMapLikeType() || container.isCollectionLikeType()) {
            for (int i = 0; i < container.containedTypeCount(); i++) {
                if (isOrContains(container.containedType(i), target)) return true;
            }
        }

        for (JavaType bound : container.getBindings().getTypeParameters()) {
            if (isOrContains(bound, target)) return true;
        }

        return false;
    }
}
