package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.PhotoService;
import com.matmuh.matmuhsite.business.constants.PhotoMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.PhotoDao;
import com.matmuh.matmuhsite.entities.Photo;
import com.matmuh.matmuhsite.entities.dtos.RequestPhotoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotoManager implements PhotoService {

    private final PhotoDao photoDao;

    @Autowired
    public PhotoManager(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    @Override
    public Result addPhoto(RequestPhotoDto requestPhotoDto) {

        if(requestPhotoDto.getPhotoUrl() == null){
            return new SuccessResult(PhotoMessages.photoUrlCanotBeNull);
        }

        Photo photo = Photo.builder()
                .photoUrl(requestPhotoDto.getPhotoUrl())
                .build();

        photoDao.save(photo);
        return new SuccessResult(PhotoMessages.photoAddSuccess);
    }


    @Override
    public DataResult<List<Photo>> getPhotos() {
        var result = photoDao.findAll();

        if(result.isEmpty()){
            return new ErrorDataResult<>(PhotoMessages.getPhotosEmpty);
        }

        return new SuccessDataResult<List<Photo>>(result, PhotoMessages.getPhotosSuccess);
    }

    @Override
    public DataResult<Photo> getPhotoById(int id) {
        var result = photoDao.findById(id);

        if(result == null){
            return new ErrorDataResult<>(PhotoMessages.getPhotosEmpty);
        }

        return new SuccessDataResult<Photo>(result, PhotoMessages.getPhotoSuccess);
    }

    @Override
    public Result deletePhoto(int id) {
        return null;
    }

}
