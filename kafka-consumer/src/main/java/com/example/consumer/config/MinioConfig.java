package com.example.consumer.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint:http://minio:9000}")
    private String minioEndpoint;

    @Value("${minio.access-key:admin}")
    private String minioAccessKey;

    @Value("${minio.secret-key:adminadmin!!}")
    private String minioSecretKey;

    @Value("${minio.bucket:default}")
    private String minioBucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();

        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
                log.info("Created MinIO bucket: {}", minioBucket);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Error initializing MinIO client: {}", e.getMessage(), e);
        }

        return minioClient;
    }

    @Bean
    public String minioBucket() {
        return minioBucket;
    }
}