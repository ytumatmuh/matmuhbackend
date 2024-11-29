package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/addImage")
    public ResponseEntity<DataResult<Image>> addImage(@RequestParam("image") Optional<MultipartFile> image){

        if (!image.isPresent()){
            var error = new ErrorDataResult<Image>(ImageMessages.photoCanotBeNull, HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(error.getHttpStatus()).body(error);
        }

        var result = imageService.addImage(image.get());
        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(result);
        }
        result.getData().setData(null);
        result.getData().setUrl(API_URL+IMAGE_GET_URL+result.getData().getUrl());


        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    /*
    @GetMapping("/getImageById/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable Optional<Integer> id){
        var image = imageService.getImageById(id.get());

        return ResponseEntity.ok().contentType(MediaType.valueOf(image.getData().getImageType())).body(image.getData().getImageData());
    }
     */

    @GetMapping("/getImageByUrl/{url}")
    public ResponseEntity<byte[]> getImageByUrl(@PathVariable Optional<String> url) {

        if (!url.isPresent()){
            var error = new ErrorResult(ImageMessages.photoUrlCanotBeNull, HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(error.getHttpStatus()).build();
        }

        var imageResult = imageService.getImageByImageUrl(url.get());

        if(imageResult.isSuccess()) {
            return ResponseEntity.status(imageResult.getHttpStatus())
                    .contentType(MediaType.valueOf(imageResult.getData().getType()))
                    .body(imageResult.getData().getData());
        }else{
            return ResponseEntity.status(imageResult.getHttpStatus()).build();
        }

    }

    @GetMapping("/getImageDetailsByUrl/{url}")
    public ResponseEntity<DataResult<Image>> getImageDetailsByUrl(@PathVariable Optional<String> url){

        if (!url.isPresent()){
            var error = new ErrorDataResult<Image>(ImageMessages.photoUrlCanotBeNull, HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(error.getHttpStatus()).body(error);
        }

        var imageResult = imageService.getImageByImageUrl(url.get());

        if (!imageResult.isSuccess()){
            return ResponseEntity.status(imageResult.getHttpStatus()).body(imageResult);
        }

        return ResponseEntity.status(imageResult.getHttpStatus()).body(imageResult);
    }

}
