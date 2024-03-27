package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.PhotoService;
import com.matmuh.matmuhsite.business.constants.PhotoMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
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
    public Result addPhoto(Photo photo) {
        photoDao.save(photo);
        return new SuccessResult(PhotoMessages.photoAddSuccess);
    }

    @Override
    public Result addPhoto(RequestPhotoDto requestPhotoDto) {
        return null;
    }

    @Override
    public DataResult<List<Photo>> getPhotos() {
        var result = photoDao.findAll();

        return new SuccessDataResult<List<Photo>>(result, PhotoMessages.getPhotosSuccess);
    }

    @Override
    public DataResult<Photo> getPhotoById(int id) {
        var result = photoDao.findById(id);

        return new SuccessDataResult<Photo>(result, PhotoMessages.getPhotoSuccess);
    }
}
