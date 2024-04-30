package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    DataResult<Image> addImage(MultipartFile file);

    DataResult<List<Image>> getImages();

    DataResult<Image> getImageById(int id);

    Result deleteImage(int id);

    DataResult<Image> getImageByImageUrl(String url);


}
