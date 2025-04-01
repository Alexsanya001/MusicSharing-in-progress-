package com.example.musicsharing.services;


import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface ObjectStorageService {
    void uploadFile(String key, InputStream inputStream, long size);

    CompletableFuture<InputStream> getFile(String key);

    void deleteFile(String key);

    String getPublicUrl(String key);

    String getPreSignedUrl(String key, Duration duration);
}
