package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.webAPI.dtos.researches.RequestResearchDto;
import com.matmuh.matmuhsite.webAPI.dtos.researches.ResponseResearchDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/researches")
public class ResearchController {

    private final ResearchService researchService;

    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @PostMapping("/addResearch")
    public ResponseEntity<Result> addResearch(
            @RequestPart("data") RequestResearchDto researchDto,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        Research research = Research.builder()
                .title(researchDto.getTitle())
                .description(researchDto.getDescription())
                .build();

        var result = researchService.addResearch(research, coverImage);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }


    @GetMapping(value = {"/getResearches", "/getResearches/{numberOfResearches}", "/getResearches/"})
    public ResponseEntity<DataResult<List<ResponseResearchDto>>> getResearches(@PathVariable Optional<Integer> numberOfResearches) {
       var result = researchService.getResearches(numberOfResearches);
       if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(result.getMessage(), result.getHttpStatus()));
       }

       List<ResponseResearchDto> responseResearchDtos = result.getData().stream().map(research -> {
           return ResponseResearchDto.builder()
                   .id(research.getId())
                   .title(research.getTitle())
                   .description(research.getDescription())
                   .coverImageUrl(research.getCoverImage() != null ? API_URL + IMAGE_GET_URL + research.getCoverImage().getUrl() : null)
                   .build();
       }).toList();


       return ResponseEntity.status(result.getHttpStatus()).body(
               new SuccessDataResult<>(responseResearchDtos, result.getMessage(), result.getHttpStatus())
       );


    }


    @GetMapping("/getResearchById/{id}")
    public ResponseEntity<DataResult<ResponseResearchDto>> getResearchById(@PathVariable UUID id) {
        var result = researchService.getResearchById(id);

        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
            );
        }

        ResponseResearchDto responseResearchDto = ResponseResearchDto.builder()
                .id(result.getData().getId())
                .title(result.getData().getTitle())
                .description(result.getData().getDescription())
                .coverImageUrl(result.getData().getCoverImage() != null ? API_URL + IMAGE_GET_URL + result.getData().getCoverImage().getUrl() : null)
                .build();

        return ResponseEntity.status(result.getHttpStatus()).body(
                new SuccessDataResult<>(responseResearchDto, result.getHttpStatus())
        );

    }

    @PutMapping("/updateResearchById/{id}")
    public ResponseEntity<Result> updateResearch(
            @PathVariable Optional<UUID> id,
            @RequestPart("data") RequestResearchDto researchDto,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        Research research = Research.builder()
                .id(id.get())
                .title(researchDto.getTitle())
                .description(researchDto.getDescription())
                .build();

        var result = researchService.updateResearch(research, coverImage);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @DeleteMapping("/deleteResearchById/{id}")
    public ResponseEntity<Result> deleteResearch(@PathVariable UUID id) {
        var result = researchService.deleteResearch(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

}
