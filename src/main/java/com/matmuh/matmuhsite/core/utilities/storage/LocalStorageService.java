package com.matmuh.matmuhsite.core.utilities.storage;

import com.matmuh.matmuhsite.business.constants.FileMessages;
import com.matmuh.matmuhsite.core.exceptions.FileDeleteException;
import com.matmuh.matmuhsite.core.exceptions.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path rootPath;

    public LocalStorageService(@Value("${storage.local.path:uploads}") String rootPath) {
        this.rootPath = Path.of(rootPath).toAbsolutePath().normalize();
    }

    @Override
    public String uploadFile(byte[] fileBytes, String originalFileName, String contentType, FolderType folderType) {
        String sanitizedName = originalFileName == null ? "file" : originalFileName.replaceAll("\\s+", "_");
        String folder = folderType == FolderType.IMAGE ? "images" : "files";
        String key = folder + "/" + UUID.randomUUID() + "-" + sanitizedName;

        try {
            Path target = rootPath.resolve(key).normalize();
            if (!target.startsWith(rootPath)) {
                throw new IOException("Invalid file path: " + key);
            }
            Files.createDirectories(target.getParent());
            Files.write(target, fileBytes);
            logger.info("File stored locally: {}", target);
            return key;
        } catch (IOException e) {
            logger.error("Local file store failed for {}: {}", key, e.getMessage());
            throw new FileUploadException(FileMessages.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            Path target = rootPath.resolve(key).normalize();
            if (!target.startsWith(rootPath)) {
                throw new IOException("Invalid file path: " + key);
            }
            Files.deleteIfExists(target);
            logger.info("Local file deleted: {}", target);
        } catch (IOException e) {
            logger.error("Local file delete failed for {}: {}", key, e.getMessage());
            throw new FileDeleteException(FileMessages.FILE_DELETE_ERROR);
        }
    }
}
