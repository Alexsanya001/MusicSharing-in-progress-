package com.example.musicsharing.services.impl;

import com.example.musicsharing.services.AbstractS3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service             // @TODO Final class in prod after AOP logging disabled
@ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "minio", matchIfMissing = true)
public class MinioStorageService extends AbstractS3StorageService {

    public MinioStorageService(@Value("${storage.minio.endpoint}") String endpoint,
                               @Value("${storage.minio.access-key}") String accessKey,
                               @Value("${storage.minio.secret-key}") String secretKey,
                               @Value("${storage.minio.bucket-name}") String bucketName,
                               @Value("${storage.minio.region}") String region) {
        super(endpoint, accessKey, secretKey, bucketName, region);
    }

    @Override
    public String getPublicUrl(String key) {
        return "%s/%s/%s".formatted(endpoint, bucketName, key);
    }

    @Override
    public String getPreSignedUrl(String key, Duration duration) {
        return "";
    }
}
