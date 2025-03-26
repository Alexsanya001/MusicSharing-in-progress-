package com.example.musicsharing.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractS3StorageService implements ObjectStorageService {
    protected final S3Client s3Client;
    protected final String bucketName;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected AbstractS3StorageService(String endpoint, String accessKey, String secretKey, String bucketName, String region) {
        this.bucketName = bucketName;
        this.s3Client = createS3Client(endpoint, accessKey, secretKey, region);
    }

    private S3Client createS3Client(String endpoint, String accessKey, String secretKey, String region) {
        SdkHttpClient httpClient = ApacheHttpClient.builder().build();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .httpClient(httpClient)
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .forcePathStyle(true);

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Async
    @Override
    public void uploadFile(String key, File file) {
        System.setProperty("aws.sdk.logging", "true");
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(file)
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
}
