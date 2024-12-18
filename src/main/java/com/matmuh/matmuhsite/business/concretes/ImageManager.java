package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ImageDao;
import com.matmuh.matmuhsite.entities.Image;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ImageManager implements ImageService {

    private final ImageDao imageDao;

    private final UserService userService;

    public ImageManager(ImageDao imageDao, UserService userService) {
        this.imageDao = imageDao;
        this.userService = userService;
    }

    @Override
    public DataResult<Image> addImage(MultipartFile image) {

        if (image == null){
            return new ErrorDataResult<>(ImageMessages.photoCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        var userResult = userService.getAuthenticatedUser();
        if (!userResult.isSuccess()){
            return new ErrorDataResult<>(userResult.getMessage(), userResult.getHttpStatus());
        }

        try {
            Image imageToSave = Image.builder()
                    .type(image.getContentType())
                    .name(image.getOriginalFilename())
                    .data(image.getBytes())
                    .url(generateUrl())
                    .createdBy(userResult.getData())
                    .build();

            imageDao.save(imageToSave);

            return new SuccessDataResult<>(imageToSave,ImageMessages.photoAddSuccess, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ErrorDataResult<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public DataResult<List<Image>> getImages() {
        var result = imageDao.findAll();

        if(result.isEmpty()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<List<Image>>(result, ImageMessages.getPhotosSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Image> getImageById(UUID id) {
        var result = imageDao.findById(id);

        if(!result.isPresent()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty, HttpStatus.NOT_FOUND);
        }
        return new SuccessDataResult<Image>(result.get(), ImageMessages.getPhotoSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteImage(UUID id) {
        var result = this.imageDao.findById(id);

        if(!result.isPresent()){
            return new ErrorResult(ImageMessages.getPhotosEmpty, HttpStatus.NOT_FOUND);
        }

        this.imageDao.delete(result.get());
        return new SuccessResult(ImageMessages.photoDeleteSuccess, HttpStatus.OK);

    }

    public DataResult<Image> getImageByUrl(String url){
        var result = imageDao.findByUrl(url);

        if(!result.isPresent()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty, HttpStatus.NOT_FOUND);
        }


        return new SuccessDataResult<Image>(result.get(), ImageMessages.getPhotoSuccess, HttpStatus.OK);
    }

    private String generateUrl() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
