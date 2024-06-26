package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ImageDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestImageDto;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ImageManager(ImageDao imageDao) {
        this.imageDao = imageDao;
    }

    @Override
    public DataResult<Image> addImage(MultipartFile file) {
        try {
            Image image = Image.builder()
                    .imageType(file.getContentType())
                    .imageName(file.getName())
                    .imageData(file.getBytes())
                    .imageUrl(generateUrl())
                    .build();

            imageDao.save(image);
            return new SuccessDataResult<>(image,ImageMessages.photoAddSuccess);
        } catch (IOException e) {
            return new ErrorDataResult<>();
        }
    }

    private String generateUrl() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }


    @Override
    public DataResult<List<Image>> getImages() {
        var result = imageDao.findAll();

        if(result.isEmpty()){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }

        return new SuccessDataResult<List<Image>>(result, ImageMessages.getPhotosSuccess);
    }

    @Override
    public DataResult<Image> getImageById(int id) {
        var result = imageDao.findById(id);

        if(result == null){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }
        return new SuccessDataResult<Image>(result, ImageMessages.getPhotoSuccess);
    }

    @Override
    public Result deleteImage(int id) {
        var result = this.imageDao.findById(id);

        if(result == null){
            return new ErrorResult(ImageMessages.getPhotosEmpty);
        }

        this.imageDao.delete(result);
        return new SuccessResult(ImageMessages.photoDeleteSuccess);

    }

    public DataResult<Image> getImageByImageUrl(String url){
        var result = imageDao.findByImageUrl(url);

        if(result == null){
            return new ErrorDataResult<>(ImageMessages.getPhotosEmpty);
        }

        return new SuccessDataResult<Image>(result, ImageMessages.getPhotoSuccess);
    }

}
