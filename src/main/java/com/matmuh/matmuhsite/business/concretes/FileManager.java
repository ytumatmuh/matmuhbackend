package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.FileMessages;
import com.matmuh.matmuhsite.core.helpers.UploadValidator;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.storage.FolderType;
import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.dtos.image.response.ImageDto;
import com.matmuh.matmuhsite.core.exceptions.*;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.core.utilities.storage.StorageService;
import com.matmuh.matmuhsite.dataAccess.abstracts.FileDao;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileManager implements FileService {

    private final SecurityService securityService;

    private final StorageService storageService;

    private final FileDao fileDao;

    private final UploadValidator uploadValidator;

    private final Logger logger = LoggerFactory.getLogger(FileManager.class);


    public FileManager(SecurityService securityService, FileDao fileDao, StorageService storageService,
                       UploadValidator uploadValidator) {
        this.securityService = securityService;
        this.fileDao = fileDao;
        this.storageService = storageService;
        this.uploadValidator = uploadValidator;
    }

    @Override
    public FileDto uploadFile(MultipartFile file) {
        logger.info("Uploading file: {}", file.getOriginalFilename());

        uploadValidator.validate(file, FolderType.FILE,
                FileMessages.FILE_EMPTY_ERROR, FileMessages.FILE_SIZE_LIMIT);

        try {
            String fileKey = storageService.uploadFile(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    FolderType.FILE
            );

            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileType(file.getContentType());
            newFile.setFileUrl(fileKey);
            newFile.setFileSize(file.getSize());

            var savedFile = fileDao.save(newFile);
            logger.info("File uploaded successfully: {}", savedFile.getFileName());

            FileDto fileDto = new FileDto();
            fileDto.setId(savedFile.getId());
            fileDto.setFileName(savedFile.getFileName());
            fileDto.setFileType(savedFile.getFileType());
            fileDto.setFileUrl(savedFile.getFileUrl());
            fileDto.setFileSize(savedFile.getFileSize());

            return fileDto;



        } catch (IOException e) {
            logger.error("File upload failed (IO Error) fileName: {}, errorMessage: {}", file.getOriginalFilename(), e.getMessage());
            throw new FileUploadException(FileMessages.FILE_UPLOAD_ERROR);
        }catch (Exception e){
            logger.error("File upload failed (General) fileName: {}, errorMessage: {}", file.getOriginalFilename(), e.getMessage());
            throw new FileUploadException(FileMessages.FILE_UPLOAD_ERROR);
        }


    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {
        logger.info("Deleting file with ID: {}", fileId);

        var fileOptional = fileDao.findById(fileId);

        if (fileOptional.isEmpty()){
            logger.error("File deletion failed: File with ID {} not found.", fileId);
            throw new ResourceNotFoundException(FileMessages.FILE_NOT_FOUND_ERROR);
        }

        File fileToDelete = fileOptional.get();

        User user = securityService.getAuthenticatedUserFromContext();

        boolean isOwner = fileToDelete.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getAuthorities().contains(Role.ROLE_ADMIN);

        if (!isAdmin && !isOwner){
            logger.error("File deletion failed: User {} does not have permission to delete file with ID {}. File creator ID: {}", user.getId(), fileId, fileToDelete.getCreatedBy().getId());
            throw new PermissionDeniedException(FileMessages.FILE_PERMISSION_ERROR);
        }

        fileDao.deleteById(fileId);

        logger.info("File with ID {} soft deleted by userID {}", fileId, user.getId());
    }

    @Override
    public File getReference(UUID id) {
        logger.info("Getting reference for file with ID: {}", id);

        return fileDao.getReferenceById(id);
    }
}
