package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ImageDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class ImageManager implements ImageService {

    private final ImageDao imageDao;

    private final UserService userService;

    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;

    public ImageManager(ImageDao imageDao, UserService userService) {
        this.imageDao = imageDao;
        this.userService = userService;
    }

    @Override
    public DataResult<Image> addImage(MultipartFile image) {


        var userResult = userService.getAuthenticatedUser();
        if (!userResult.isSuccess()){
            return new ErrorDataResult<>(null, userResult.getMessage());
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

            //image.setImageUrl(API_URL+IMAGE_GET_URL+image.getImageUrl());

            return new SuccessDataResult<>(imageToSave,ImageMessages.photoAddSuccess);
        } catch (IOException e) {
            return new ErrorDataResult<>(e.getMessage());
        }
    }


    @Override
    public DataResult<List<Image>> getImages() {
        var result = imageDao.findAll();

        if(result.isEmpty()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }

        result.forEach(image -> image.setUrl(API_URL+IMAGE_GET_URL+image.getUrl()));

        return new SuccessDataResult<List<Image>>(result, ImageMessages.getPhotosSuccess);
    }

    @Override
    public DataResult<Image> getImageById(int id) {
        var result = imageDao.findById(id);

        if(!result.isPresent()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }

        result.get().setUrl(API_URL+IMAGE_GET_URL+result.get().getUrl());
        return new SuccessDataResult<Image>(result.get(), ImageMessages.getPhotoSuccess);
    }

    @Override
    public Result deleteImage(int id) {
        var result = this.imageDao.findById(id);

        if(!result.isPresent()){
            return new ErrorResult(ImageMessages.getPhotosEmpty);
        }

        this.imageDao.delete(result.get());
        return new SuccessResult(ImageMessages.photoDeleteSuccess);

    }

    public DataResult<Image> getImageByImageUrl(String url){
        var result = imageDao.findByUrl(url);

        if(!result.isPresent()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }

        result.get().setUrl(API_URL+IMAGE_GET_URL+result.get().getUrl());

        return new SuccessDataResult<Image>(result.get(), ImageMessages.getPhotoSuccess);
    }

    private String generateUrl() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
