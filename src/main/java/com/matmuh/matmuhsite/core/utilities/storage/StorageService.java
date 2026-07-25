package com.matmuh.matmuhsite.core.utilities.storage;

public interface StorageService {

    String uploadFile(byte[] fileBytes, String originalFileName, String contentType, FolderType folderType);

    void deleteFile(String key);
}
