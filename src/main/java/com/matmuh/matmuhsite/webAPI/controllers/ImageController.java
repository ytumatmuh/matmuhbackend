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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;


    @PostMapping("/addImage")
    public ResponseEntity<DataResult<Image>> addImage(@RequestParam("image") Optional<MultipartFile> image){

        if (!image.isPresent()){
            return ResponseEntity.badRequest().body(new ErrorDataResult<>(ImageMessages.photoCanotBeNull));
        }

        var result = imageService.addImage(image.get());

        if (!result.isSuccess()){
            return ResponseEntity.badRequest().body(new ErrorDataResult<>(result.getMessage()));
        }
        result.getData().setData(null);
        result.getData().setUrl(API_URL+IMAGE_GET_URL+result.getData().getUrl());


        return ResponseEntity.ok().body(result);
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
            return ResponseEntity.badRequest().body(ImageMessages.photoUrlCanotBeNull.getBytes());
        }

        var image = imageService.getImageByImageUrl(url.get());

        if(image.isSuccess()) {
            return ResponseEntity.ok().contentType(MediaType.valueOf(image.getData().getType())).body(image.getData().getData());
        }else{
            return ResponseEntity.status(404).build();
        }

    }

    @GetMapping("/getImageDetailsByUrl/{url}")
    public ResponseEntity<DataResult<Image>> getImageDetailsByUrl(@PathVariable Optional<String> url){

        if (!url.isPresent()){
            return ResponseEntity.badRequest().body(new ErrorDataResult<>(ImageMessages.photoUrlCanotBeNull));
        }

        var imageResult = imageService.getImageByImageUrl(url.get());



        if (!imageResult.isSuccess()){
            return ResponseEntity.badRequest().body(imageResult);
        }
        imageResult.getData().setData(null);

        return ResponseEntity.ok().body(imageResult);



    }

}
