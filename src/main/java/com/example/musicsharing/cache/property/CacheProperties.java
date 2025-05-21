package com.example.musicsharing.cache.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Field;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private CaffeineProperties caffeine = new CaffeineProperties();
    private RedisProperties redis = new RedisProperties();

    public static void validate(Object instance) {
        try {
            Class<?> clazz = instance.getClass();
            Field[] fields = clazz.getDeclaredFields();

            Field enabledField = clazz.getDeclaredField("enabled");
            enabledField.setAccessible(true);
            Boolean enabled = (Boolean) enabledField.get(instance);

            if (Boolean.TRUE.equals(enabled)) {

                String prefix = switch (instance.getClass().getSimpleName()) {
                    case "CaffeineProperties" -> "cache.caffeine";
                    case "RedisProperties" -> "cache.redis";
                    default -> null;
                };

                for (Field field : fields) {
                    if ("enabled".equals(field.getName())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(instance);
                    String fieldName = field.getName();
                    String propertyName = prefix + "." + fieldName;

                    if (value == null) {
                        throw new IllegalStateException("Missing required property " + propertyName);
                    }

                    if (value instanceof Number number) {
                        if (number.longValue() <= 0) {
                            throw new IllegalArgumentException("Property " + propertyName + " must be positive");
                        }
                    }

                    if (value instanceof Duration duration) {
                        if (duration.isNegative() || duration.isZero()) {
                            throw new IllegalArgumentException("Property" + propertyName + " must be positive");
                        }
                    }

                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    public static class CaffeineProperties {
        private boolean enabled;
        private Duration ttl;
        private Long maxSize;
    }

    @Getter
    @Setter
    public static class RedisProperties {
        private boolean enabled;
        private Duration ttl;
    }
}