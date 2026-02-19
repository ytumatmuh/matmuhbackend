package com.matmuh.matmuhsite.core.utilities.storage;

import com.matmuh.matmuhsite.core.config.r2.FolderType;
import com.matmuh.matmuhsite.core.properties.R2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
public class R2StorageService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;
    private final Logger logger = LoggerFactory.getLogger(R2StorageService.class);


    public R2StorageService(S3Client s3Client, R2Properties r2Properties) {
        this.s3Client = s3Client;
        this.r2Properties = r2Properties;
    }


    public String uploadFile(byte[] fileBytes, String originalFileName, String contentType, FolderType folderType){

        logger.info("Uploading file: {}, contentType: {}, folderType: {}", originalFileName, contentType, folderType);
        String sanitizedName = originalFileName.replaceAll("\\s+", "_");

        String key;
        if (folderType == FolderType.IMAGE) {
            key = "images/" + UUID.randomUUID().toString()+ "-"+ sanitizedName;
            logger.info("Generated key for image: {}", key);
        }else if (folderType == FolderType.FILE){
            key = "files/" + UUID.randomUUID().toString() + "-" + sanitizedName;
            logger.info("Generated key for file: {}", key);
        } else {
            logger.warn("Invalid folder type provided: {}", folderType);
            throw new IllegalArgumentException("Invalid folder type: " + folderType);
        }


        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

        logger.info("File uploaded successfully: {}", key);

        return key;
    }

    public void deleteFile(String key){
        logger.info("Deleting file: {}", key);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(key)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully: {}", key);
        }catch (Exception e){
            logger.error("Error deleting file: {}, error: {}", key, e.getMessage());
            throw e;
        }

    }
}
