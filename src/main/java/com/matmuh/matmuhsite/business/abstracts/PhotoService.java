package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Photo;
import com.matmuh.matmuhsite.entities.dtos.RequestPhotoDto;

import java.util.List;

public interface PhotoService {

    Result addPhoto(RequestPhotoDto requestPhotoDto);

    DataResult<List<Photo>> getPhotos();

    DataResult<Photo> getPhotoById(int id);


}
