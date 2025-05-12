package com.example.musicsharing.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class TypeUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getTypeDescription(TypeReference<?> ref) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(ref);
        if (Map.class.isAssignableFrom(javaType.getRawClass())) {
            JavaType keyType = javaType.getKeyType();
            JavaType valueType = javaType.getContentType();
            return "Map<" + keyType.getRawClass().getSimpleName() + "," + valueType.getRawClass().getSimpleName() + ">";
        } else if (Collection.class.isAssignableFrom(javaType.getRawClass())) {
            JavaType contentType = javaType.getContentType();
            return "Collection<" + contentType.getRawClass().getSimpleName() + ">";
        } else {
            return javaType.getRawClass().getSimpleName();
        }
    }

    public static JavaType toJavaType(Object value) {
       TypeFactory tf = TypeFactory.defaultInstance();

       switch (value) {
           case null -> {
               return tf.constructType(Object.class);
           }
           case Collection<?> collection when !collection.isEmpty() -> {
               Class<?> elementType = collection.iterator().next().getClass();
               return tf.constructCollectionType(Collection.class, elementType);
           }
           case Map<?, ?> map when !map.isEmpty() -> {
               Map.Entry<?, ?> entry = map.entrySet().iterator().next();
               Class<?> keyType = entry.getKey().getClass();
               Class<?> valueType = entry.getValue().getClass();
               return tf.constructMapType(Map.class, keyType, valueType);
           }
           default -> {
               return tf.constructType(value.getClass());
           }
       }
    }

   public static JavaType toJavaType(TypeReference<?> ref) {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType javaType = tf.constructType(ref);

        switch (javaType) {
            case
        }
   }
}
