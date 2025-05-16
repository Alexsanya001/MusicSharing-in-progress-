package com.example.musicsharing.cache.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public final class TypeUtils {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();


    public static JavaType toJavaType(TypeReference<?> ref) {
        Objects.requireNonNull(ref, "TypeReference must not be null");
        return TYPE_FACTORY.constructType(ref.getType());
    }


}
