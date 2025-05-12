package com.example.musicsharing.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractS3StorageService implements ObjectStorageService {
    S3Client s3Client;
    String bucketName;
    String endpoint;
    String region;

    protected AbstractS3StorageService(String endpoint, String accessKey, String secretKey, String bucketName, String region) {
        this.bucketName = bucketName;
        this.s3Client = createS3Client(endpoint, accessKey, secretKey, region);
        this.endpoint = endpoint;
        this.region = region;
    }

    private S3Client createS3Client(String endpoint, String accessKey, String secretKey, String region) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .forcePathStyle(true);

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Async
    @Override
    public void uploadFile(String key, InputStream inputStream, long size) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromInputStream(inputStream, size)
        );
    }

    @Async
    @Override
    public CompletableFuture<InputStream> getFile(String key) {
        return CompletableFuture.completedFuture(s3Client.getObject(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                ResponseTransformer.toInputStream()
        ));
    }

    @Async
    @Override
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public abstract String getPublicUrl(String key);

    @Override
    public abstract String getPreSignedUrl(String key, Duration duration);
}
