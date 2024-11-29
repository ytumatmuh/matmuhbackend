package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.User;
import com.matmuh.matmuhsite.entities.dtos.announcements.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.announcements.ResponseAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.users.ResponseUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping("/addAnnouncement")
    public ResponseEntity<Result> addAnnouncement(
            @RequestPart("data") RequestAnnouncementDto announcementDto,
            @RequestPart("coverImage") Optional<MultipartFile> coverImage) {

        Announcement announcementToSave = Announcement.builder()
                .title(announcementDto.getTitle())
                .content(announcementDto.getContent())
                .build();

        var addAnnouncementResult = announcementService.addAnnouncement(announcementToSave, coverImage);

        return ResponseEntity.status(addAnnouncementResult.getHttpStatus()).body(addAnnouncementResult);
    }



    @GetMapping(value = {"/getAnnouncements/{numberOfAnnouncements}", "/getAnnouncements"})
    public ResponseEntity<DataResult<List<ResponseAnnouncementDto>>> getAnnouncements(@PathVariable Optional<Integer> numberOfAnnouncements){

        var result = announcementService.getAnnouncements(numberOfAnnouncements);
        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
            );
        }

        List<ResponseAnnouncementDto> announcementsToReturn = result.getData().stream().map(announcement -> {
            ResponseUserDto publisherToReturnDto = ResponseUserDto.builder()
                    .id(announcement.getPublisher().getId())
                    .email(announcement.getPublisher().getEmail())
                    .firstName(announcement.getPublisher().getFirstName())
                    .lastName(announcement.getPublisher().getLastName())
                    .build();

            return ResponseAnnouncementDto.builder()
                    .id(announcement.getId())
                    .title(announcement.getTitle())
                    .content(announcement.getContent())
                    .coverImageUrl(announcement.getCoverImage().getUrl())
                    .publisher(publisherToReturnDto)
                    .publishDate(announcement.getPublishDate())
                    .build();
        }).toList();

        return ResponseEntity.status(result.getHttpStatus()).body(
                new SuccessDataResult<>(announcementsToReturn, result.getMessage(), result.getHttpStatus())
        );
    }




    @GetMapping("/getAnnouncementById/{id}")
    public ResponseEntity<DataResult<ResponseAnnouncementDto>> getAnnouncementById(@PathVariable UUID id){

        var result = announcementService.getAnnouncementById(id);
        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
            );
        }

        ResponseUserDto publisherToReturnDto = ResponseUserDto.builder()
                .id(result.getData().getPublisher().getId())
                .email(result.getData().getPublisher().getEmail())
                .firstName(result.getData().getPublisher().getFirstName())
                .lastName(result.getData().getPublisher().getLastName())
                .build();

        ResponseAnnouncementDto announcementToReturn = ResponseAnnouncementDto.builder()
                .id(result.getData().getId())
                .title(result.getData().getTitle())
                .content(result.getData().getContent())
                .coverImageUrl(result.getData().getCoverImage().getUrl())
                .publisher(publisherToReturnDto)
                .publishDate(result.getData().getPublishDate())
                .build();

        return ResponseEntity.status(result.getHttpStatus()).body(
                new SuccessDataResult<>(announcementToReturn, result.getMessage(), result.getHttpStatus())
        );

    }
}
