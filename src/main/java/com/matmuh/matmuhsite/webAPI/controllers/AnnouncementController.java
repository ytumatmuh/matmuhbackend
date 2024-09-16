package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseAnnouncementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/addAnnouncement")
    public Result addAnnouncement(
            @RequestPart("data") RequestAnnouncementDto announcementDto,
            @RequestPart("coverImage") MultipartFile coverImage) {
        return announcementService.addAnnouncement(announcementDto, coverImage);
    }
    @GetMapping(value = {"/getAnnouncements/{numberOfAnnouncements}", "/getAnnouncements"})
    public Result getAnnouncements(@PathVariable Optional<Integer> numberOfAnnouncements){
        return announcementService.getAnnouncements(numberOfAnnouncements);
    }
    @GetMapping("/getAnnouncementById/{id}")
    public DataResult<ResponseAnnouncementDto> getAnnouncementById(@PathVariable int id){
        return announcementService.getAnnouncementById(id);

    }
}
