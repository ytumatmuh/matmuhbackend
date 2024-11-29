package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.constants.AnnouncementMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementDao;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseAnnouncementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class AnnouncementManager implements AnnouncementService {

    private final AnnouncementDao announcementDao;

    private final ImageService imageService;


    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;


    public AnnouncementManager(AnnouncementDao announcementDao, ImageService imageService) {
        this.announcementDao = announcementDao;
        this.imageService = imageService;
    }

    @Override
    public Result addAnnouncement(Announcement announcement, Optional<MultipartFile> coverImage) {
        if (announcement.getTitle() == null){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(announcement.getContent() == null){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull, HttpStatus.BAD_REQUEST);
        }

         Image announcementImage = null;

        if(coverImage.isPresent()){
            var imageResult = imageService.addImage(coverImage.get());
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            announcementImage = imageResult.getData();
        }
        announcement.setCoverImage(announcementImage);


        announcementDao.save(announcement);
        return new SuccessResult(AnnouncementMessages.announcementAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public Result updateAnnouncement(Announcement announcement) {

        announcementDao.save(announcement);

        return new SuccessResult(AnnouncementMessages.announcementUpdateSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<List<Announcement>> getAnnouncements(Optional<Integer> numberOfAnnouncements) {
            List<Announcement> result = new ArrayList<>();
        if (numberOfAnnouncements.isPresent()){
            result = announcementDao.findAll(PageRequest.of(0, numberOfAnnouncements.get())).toList();
        }else{
            result = announcementDao.findAll();
        }

        if (result == null){
            return new ErrorDataResult<>(AnnouncementMessages.annoucementsNotFound, HttpStatus.NOT_FOUND);
        }



        return new SuccessDataResult<List<Announcement>>(result, AnnouncementMessages.getAnnouncementsSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Announcement> getAnnouncementById(UUID id) {
        var result = announcementDao.findById(id);

        if(result.isEmpty()){
            return new ErrorDataResult<>(AnnouncementMessages.announcementNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<Announcement>(result.get(), AnnouncementMessages.getAnnouncementByIdSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteAnnouncement(UUID id) {
        var result = this.announcementDao.findById(id);

        if (result.isEmpty()){
            return new ErrorResult(AnnouncementMessages.announcementNotFound, HttpStatus.NOT_FOUND);
        }

        this.announcementDao.delete(result.get());
        return new SuccessResult(AnnouncementMessages.announcementDeleteSuccess, HttpStatus.OK);
    }
}
