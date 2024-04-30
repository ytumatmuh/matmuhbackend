package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import org.springframework.beans.factory.annotation.Autowired;
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


    /*

    @PostMapping("/addImage")
    public Result addImage(@RequestParam MultipartFile file){
        return imageService.addImage(file);
    }

    @GetMapping("/getImageById/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable Optional<Integer> id){
        var image = imageService.getImageById(id.get());

        return ResponseEntity.ok().contentType(MediaType.valueOf(image.getData().getImageType())).body(image.getData().getImageData());
    }

     */
    @GetMapping("/getImageByUrl/{url}")
    public ResponseEntity<byte[]> getImageByUrl(@PathVariable Optional<String> url) {
        var image = imageService.getImageByImageUrl(url.get());

        if(image.isSuccess()) {
            return ResponseEntity.ok().contentType(MediaType.valueOf(image.getData().getImageType())).body(image.getData().getImageData());
        }else{
            return ResponseEntity.notFound().build();
        }

    }
}
