package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.AnnouncementMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementDao;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnnouncementManager implements AnnouncementService {

    private final AnnouncementDao announcementDao;

    private final ImageService imageService;

    private final UserService userService;



    public AnnouncementManager(AnnouncementDao announcementDao, ImageService imageService, UserService userService) {
        this.announcementDao = announcementDao;
        this.imageService = imageService;
        this.userService = userService;
    }

    @Override
    public Result addAnnouncement(Announcement announcement, MultipartFile coverImage) {
        var authenticatedUserResult = userService.getAuthenticatedUser();
        if (!authenticatedUserResult.isSuccess()){
            return authenticatedUserResult;
        }

        if (announcement.getTitle().isEmpty()){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(announcement.getContent().isEmpty()){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull, HttpStatus.BAD_REQUEST);
        }

         Image announcementImage = null;

        if(coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            announcementImage = (Image) imageResult.getData();
        }
        announcement.setCoverImage(announcementImage);
        announcement.setPublishDate(LocalDateTime.now());
        announcement.setPublisher(authenticatedUserResult.getData());

        announcementDao.save(announcement);
        return new SuccessResult(AnnouncementMessages.announcementAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public Result updateAnnouncement(Announcement announcement, MultipartFile coverImage) {

        var result = announcementDao.findById(announcement.getId());
        if (result.isEmpty()){
            return new ErrorResult(AnnouncementMessages.announcementNotFound, HttpStatus.NOT_FOUND);
        }

        if (coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            announcement.setCoverImage(imageResult.getData());
        }else {
            announcement.setCoverImage(result.get().getCoverImage());
        }

        var authenticatedUserResult = userService.getAuthenticatedUser();
        if (!authenticatedUserResult.isSuccess()){
            return authenticatedUserResult;
        }

        announcement.setPublisher(authenticatedUserResult.getData());
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
