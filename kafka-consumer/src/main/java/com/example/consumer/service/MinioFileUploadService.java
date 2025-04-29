package com.example.consumer.service;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioFileUploadService {

    private final MinioClient minioClient;
    
    public void uploadFileToMinio(String fileName, String bucket) throws MinioException, IOException, 
                                                                         InvalidKeyException, NoSuchAlgorithmException {
        File file = new File(fileName);
        
        if (!file.exists()) {
            log.warn("File {} does not exist, skipping upload", fileName);
            return;
        }
        
        String objectName = file.getName();
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .filename(fileName)
                        .build()
        );
        log.info("Uploaded file {} to MinIO bucket {}", fileName, bucket);

        Files.delete(Paths.get(fileName));
        log.info("Deleted file {} after successful upload to MinIO", fileName);
    }
}