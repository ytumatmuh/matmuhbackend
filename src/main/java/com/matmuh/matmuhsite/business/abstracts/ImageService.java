package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageService {

    DataResult<Image> addImage(MultipartFile file);

    DataResult<List<Image>> getImages();

    DataResult<Image> getImageById(UUID id);

    Result deleteImage(UUID id);

    DataResult<Image> getImageByImageUrl(String url);


}
