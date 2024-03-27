package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;
    @PostMapping("/add")
    public Result addAnnouncement(@RequestBody RequestAnnouncementDto announcementDto) {

        return announcementService.addAnnouncement(announcementDto);
    }
    @GetMapping("/getAll")
    public Result getAnnouncements(){

        return announcementService.getAnnouncements();
    }
    @GetMapping("/getById")
    public DataResult<Announcement> getAnnouncementById(@RequestParam int id){
        return announcementService.getAnnouncementById(id);

    }
    
}
