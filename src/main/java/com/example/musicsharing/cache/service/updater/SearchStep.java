package com.example.musicsharing.cache.service.updater;

public sealed interface SearchStep permits MapKey, MapValue, CollectionElement, DirectHit {
}
record MapKey() implements SearchStep {}
record MapValue() implements SearchStep {}
record CollectionElement() implements SearchStep {}
record DirectHit() implements SearchStep {}