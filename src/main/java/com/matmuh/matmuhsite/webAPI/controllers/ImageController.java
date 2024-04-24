package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;



    @PostMapping("/addImage")
    public Result addImage(@RequestParam MultipartFile file){
        return imageService.addImage(file);
    }

    @GetMapping("/getImageById")
    public ResponseEntity<byte[]> getImageById(int id){
        var image = imageService.getImageById(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf(image.getData().getImageType())).body(image.getData().getImageData());


    }


}
