package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.business.constants.FileMessages;
import com.matmuh.matmuhsite.core.exceptions.FileEmptyException;
import com.matmuh.matmuhsite.core.exceptions.FileSizeExceededException;
import com.matmuh.matmuhsite.core.exceptions.UnsupportedFileTypeException;
import com.matmuh.matmuhsite.core.properties.UploadProperties;
import com.matmuh.matmuhsite.core.utilities.storage.FolderType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class UploadValidator {

    private final UploadProperties uploadProperties;

    public UploadValidator(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public void validate(MultipartFile file, FolderType folderType, String emptyMessageKey, String sizeMessageKey) {
        if (file == null || file.isEmpty()) {
            throw new FileEmptyException(emptyMessageKey);
        }

        var maximumBytes = maximumBytes(folderType);
        if (file.getSize() > maximumBytes) {
            throw new FileSizeExceededException(sizeMessageKey, maximumBytes / (1024 * 1024));
        }

        var allowed = allowedExtensions(folderType);
        var extension = extensionOf(file.getOriginalFilename());

        if (extension == null || !allowed.contains(extension)) {
            throw new UnsupportedFileTypeException(FileMessages.fileTypeNotSupported,
                    extension == null ? "-" : extension, String.join(", ", allowed));
        }
    }

    public boolean isAllowed(MultipartFile file, FolderType folderType) {
        if (file == null) {
            return false;
        }

        var extension = extensionOf(file.getOriginalFilename());
        return extension != null && allowedExtensions(folderType).contains(extension);
    }


    private String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }

        var dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private Set<String> allowedExtensions(FolderType folderType) {
        return switch (folderType) {
            case IMAGE -> uploadProperties.getImageExtensions();
            case PUBLIC_FILE -> uploadProperties.getPublicFileExtensions();
            case FILE -> uploadProperties.getFileExtensions();
        };
    }

    private long maximumBytes(FolderType folderType) {
        var megabytes = folderType == FolderType.IMAGE
                ? uploadProperties.getMaxImageSizeMb()
                : uploadProperties.getMaxFileSizeMb();

        return (long) megabytes * 1024 * 1024;
    }
}
