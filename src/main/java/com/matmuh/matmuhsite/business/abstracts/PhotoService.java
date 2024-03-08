package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Photo;

import java.util.List;

public interface PhotoService {

    Result addPhoto(Photo photo);

    DataResult<List<Photo>> getPhotos();

    DataResult<Photo> getPhotoById(int id);


}
