package com.b2g.readerservice.service;

import io.minio.*;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void init() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    // Upload (solo admin)
    public String uploadFile(UUID bookId, MultipartFile file) throws Exception {
        // Nome fisso per ogni libro
        String filename = bookId + "-cover.jpg";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return filename;
    }

    // Download diretto in byte[]
    public byte[] downloadFile(UUID bookId) throws Exception {
        String filename = bookId + "-cover.jpg";

        try (GetObjectResponse obj = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .build());
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            byte[] buf = new byte[8192];
            int bytesRead;
            while ((bytesRead = obj.read(buf)) != -1) {
                os.write(buf, 0, bytesRead);
            }
            return os.toByteArray();
        }
    }
}