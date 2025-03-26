package com.example.musicsharing.services.impl;

import com.example.musicsharing.services.AbstractS3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "aws")
public final class AwsStorageService extends AbstractS3StorageService {

    public AwsStorageService(@Value("${storage.aws.access-key}") String accessKey,
                             @Value("${storage.aws.secret-key}") String secretKey,
                             @Value("${storage.aws.bucket-name}") String bucketName,
                             @Value("${storage.aws.region}") String region) {
        super(null, accessKey, secretKey, bucketName, region);
    }
}
