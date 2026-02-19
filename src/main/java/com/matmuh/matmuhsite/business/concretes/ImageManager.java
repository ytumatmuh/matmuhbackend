package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.config.r2.FolderType;
import com.matmuh.matmuhsite.core.dtos.image.response.ImageDto;
import com.matmuh.matmuhsite.core.exceptions.*;
import com.matmuh.matmuhsite.core.utilities.storage.R2StorageService;
import com.matmuh.matmuhsite.dataAccess.abstracts.ImageDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageManager implements ImageService {

    private final ImageDao imageDao;

    private final SecurityService securityService;

    private final R2StorageService r2StorageService;

    private final Logger logger = LoggerFactory.getLogger(ImageManager.class);

    public ImageManager(ImageDao imageDao, SecurityService securityService, R2StorageService r2StorageService) {
        this.imageDao = imageDao;
        this.securityService = securityService;
        this.r2StorageService = r2StorageService;
    }


    @Override
    public ImageDto uploadImage(MultipartFile image) {
        logger.info("Uploading image file: {}", image.getOriginalFilename());
        if (image.isEmpty()) {
            logger.error("Image upload failed: Image file is empty.");
            throw new FileEmptyException(ImageMessages.IMAGE_EMPTY_ERROR);
        }

        if (image.getSize() > 50 * 1024 * 1024){
            logger.error("Image upload failed: Image file size exceeds the limit of 50 MB.");
            throw new FileSizeExceededException(ImageMessages.IMAGE_SIZE_ERROR);
        }

        try{
            byte[] cleanImageBytes = removeMetadata(image);

            String imageKey = r2StorageService.uploadFile(
                    cleanImageBytes,
                    image.getOriginalFilename(),
                    image.getContentType(),
                    FolderType.IMAGE);


            Image newImage = new Image();
            newImage.setFileName(image.getOriginalFilename());
            newImage.setFileType(image.getContentType());
            newImage.setFileUrl(imageKey);
            newImage.setFileSize((long) cleanImageBytes.length);

            var savedImage = imageDao.save(newImage);
            logger.info("Image uploaded successfully: {}", savedImage.getFileName());
            return new ImageDto(savedImage);
        } catch (Exception e) {
            logger.error("Image upload failed fileName: {}, errorMessage: {}", image.getOriginalFilename(), e.getMessage());
            throw new FileUploadException(ImageMessages.IMAGE_UPLOAD_ERROR);
        }


    }


    @Override
    public void deleteImage(UUID id) {
        logger.info("Deleting image with ID: {}", id);
        var image = imageDao.findById(id);

        if(image.isEmpty()){
            logger.error("Image deletion failed: Image with ID {} not found.", id);
            throw new ResourceNotFoundException(ImageMessages.IMAGE_NOT_FOUND_ERROR);
        }

        User user = securityService.getAuthenticatedUserFromContext();
        if (!image.get().getCreatedBy().getId().equals(user.getId()) && !user.getAuthorities().contains(Role.ROLE_ADMIN)) {
            logger.error("Image deletion failed: User {} does not have permission to delete image with ID {}. Image creator ID: {}", user.getId(), id, image.get().getCreatedBy().getId());
            throw new PermissionDeniedException(ImageMessages.IMAGE_PERMISSION_ERROR);
        }

        try {
            r2StorageService.deleteFile(image.get().getFileUrl());
            imageDao.deleteById(id);
            logger.info("Image with ID {} deleted successfully by userID {}", id, user.getId());
        } catch (Exception e) {
            logger.error("Image deletion failed for ID {}: errorMessage {}", id, e.getMessage());
            throw new ImageDeleteException(ImageMessages.IMAGE_DELETE_ERROR);
        }
    }


    private byte[] removeMetadata(MultipartFile image) throws IOException {
        BufferedImage originalImage = ImageIO.read(image.getInputStream());

        if (originalImage == null){
            throw new IOException("Image file is empty.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        String formatName = getFileExtension(image.getOriginalFilename()).substring(1);


        ImageIO.write(originalImage, formatName, outputStream);

        return outputStream.toByteArray();

    }

    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("File has no extension: " + fileName);
        }
        return fileName.substring(fileName.lastIndexOf("."));


    }



}
