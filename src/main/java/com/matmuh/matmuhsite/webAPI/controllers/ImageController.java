package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.users.ResponseUserDto;
import com.matmuh.matmuhsite.webAPI.dtos.images.ResponseImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<DataResult<ResponseImageDto>> addImage(@RequestParam("image") Optional<MultipartFile> image){
        var result = imageService.addImage(image.get());
        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<ResponseImageDto>(result.getMessage(), result.getHttpStatus()));
        }


        ResponseUserDto responseUserDto = ResponseUserDto.builder()
                .id(result.getData().getCreatedBy().getId())
                .firstName(result.getData().getCreatedBy().getFirstName())
                .lastName(result.getData().getCreatedBy().getLastName())
                .username(result.getData().getCreatedBy().getUsername())
                .email(result.getData().getCreatedBy().getEmail())
                .build();

        ResponseImageDto responseImageDto = ResponseImageDto.builder()
                .id(result.getData().getId())
                .name(result.getData().getName())
                .type(result.getData().getType())
                .url(API_URL+IMAGE_GET_URL+result.getData().getUrl())
                .createdBy(responseUserDto)
                .build();


        return ResponseEntity.status(result.getHttpStatus()).body(
                new ErrorDataResult<>(responseImageDto, result.getMessage(), result.getHttpStatus())
        );

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

        var imageResult = imageService.getImageByUrl(url.get());

        if(imageResult.isSuccess()) {
            return ResponseEntity.status(imageResult.getHttpStatus())
                    .contentType(MediaType.valueOf(imageResult.getData().getType()))
                    .body(imageResult.getData().getData());
        }else{
            return ResponseEntity.status(imageResult.getHttpStatus()).build();
        }

    }

    @GetMapping("/getImageDetailsByUrl/{url}")
    public ResponseEntity<DataResult<ResponseImageDto>> getImageDetailsByUrl(@PathVariable String url){
        var imageResult = imageService.getImageByUrl(url);

        if (!imageResult.isSuccess()){
            return ResponseEntity.status(imageResult.getHttpStatus()).body(
                    new ErrorDataResult<ResponseImageDto>(imageResult.getMessage(), imageResult.getHttpStatus())
            );
        }

        ResponseUserDto responseUserDto = ResponseUserDto.builder()
                .id(imageResult.getData().getCreatedBy().getId())
                .firstName(imageResult.getData().getCreatedBy().getFirstName())
                .lastName(imageResult.getData().getCreatedBy().getLastName())
                .username(imageResult.getData().getCreatedBy().getUsername())
                .email(imageResult.getData().getCreatedBy().getEmail())
                .build();

        ResponseImageDto responseImageDto = ResponseImageDto.builder()
                .id(imageResult.getData().getId())
                .name(imageResult.getData().getName())
                .type(imageResult.getData().getType())
                .url(API_URL+IMAGE_GET_URL+imageResult.getData().getUrl())
                .createdBy(responseUserDto)
                .build();

        return ResponseEntity.status(imageResult.getHttpStatus()).body(
                new SuccessDataResult<>(responseImageDto, imageResult.getMessage(), imageResult.getHttpStatus())
        );
    }

    @DeleteMapping("/deleteImageById/{id}")
    public ResponseEntity<Result> deleteImageById(@PathVariable UUID id){
        var result = imageService.deleteImage(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

}
