package com.example.musicsharing.cache.wrappers;

import java.time.Duration;

public record TimedValue<V>(V value, Duration ttl) {
    public long ttlNanos() {
        return ttl.toNanos();
    }
}
