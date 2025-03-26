package com.example.musicsharing.services;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface ObjectStorageService {
    void uploadFile(String key, File file);

    CompletableFuture<InputStream> getFile(String key);

    void deleteFile(String key);
}
