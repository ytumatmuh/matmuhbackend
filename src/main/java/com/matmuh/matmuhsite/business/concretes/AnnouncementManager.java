package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.business.constants.AnnouncementMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementDao;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AnnouncementManager implements AnnouncementService {


    private final AnnouncementDao announcementDao;

    @Autowired
    public AnnouncementManager(AnnouncementDao announcementDao) {
        this.announcementDao = announcementDao;
    }

    @Override
    public Result addAnnouncement(RequestAnnouncementDto announcementDto) {
        if (announcementDto.getName() == null){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull);
        }

        if(announcementDto.getContent() == null){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull);
        }


        var announcementToSave = Announcement.builder()
                .name(announcementDto.getName())
                .content(announcementDto.getContent())
                .publishDate(new Date())
                .build();


        announcementDao.save(announcementToSave);
        return new SuccessResult(AnnouncementMessages.announcementAddSuccess);
    }

    @Override
    public Result updateAnnouncement(RequestAnnouncementDto requestAnnouncementDto) {

        var announcement = announcementDao.findById(requestAnnouncementDto.getId());

        if (announcement == null){
            return new ErrorResult(AnnouncementMessages.announcementNotFound);
        }

        if(requestAnnouncementDto.getName() == null){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull);
        }

        if(requestAnnouncementDto.getContent() == null){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull);
        }

        announcement.setName(requestAnnouncementDto.getName().isEmpty() ? announcement.getName() : requestAnnouncementDto.getName());
        announcement.setContent(requestAnnouncementDto.getContent().isEmpty() ? announcement.getContent() : requestAnnouncementDto.getContent());

        announcementDao.save(announcement);

        return new SuccessResult(AnnouncementMessages.announcementUpdateSuccess);
    }

    @Override
    public DataResult<List<Announcement>> getAnnouncements() {
        var result = announcementDao.findAll();

        if (result == null){
            return new ErrorDataResult<>(AnnouncementMessages.annoucementsNotFound);
        }

        return new SuccessDataResult<List<Announcement>>(result, AnnouncementMessages.getAnnouncementsSuccess);
    }

    @Override
    public DataResult<Announcement> getAnnouncementById(int id) {
        var result = announcementDao.findById(id);

        if(result == null){
            return new ErrorDataResult<>(AnnouncementMessages.announcementNotFound);
        }

        return new SuccessDataResult<Announcement>(result, AnnouncementMessages.getAnnouncementByIdSuccess);
    }

    @Override
    public Result deleteAnnouncement(int id) {
        var result = this.announcementDao.findById(id);

        if (result == null){
            return new ErrorResult(AnnouncementMessages.announcementNotFound);
        }

        this.announcementDao.delete(result);
        return new SuccessResult(AnnouncementMessages.announcementDeleteSuccess);
    }
}
