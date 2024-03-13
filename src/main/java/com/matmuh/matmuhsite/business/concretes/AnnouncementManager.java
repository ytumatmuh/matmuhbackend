package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.business.constants.AnnouncementMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementDao;
import com.matmuh.matmuhsite.entities.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementManager implements AnnouncementService {

    private final AnnouncementDao announcementDao;

    @Autowired
    public AnnouncementManager(AnnouncementDao announcementDao) {
        this.announcementDao = announcementDao;
    }

    @Override
    public Result addAnnouncement(Announcement announcement) {
        announcementDao.save(announcement);

        return new SuccessResult(AnnouncementMessages.announcementAddSuccess);
    }

    @Override
    public DataResult<List<Announcement>> getAnnouncements() {
        var result = announcementDao.findAll();

        return new SuccessDataResult<List<Announcement>>(result, AnnouncementMessages.getAnnouncementsSuccess);
    }

    @Override
    public DataResult<Announcement> getAnnouncementById(int id) {
        var result = announcementDao.findById(id);

        return new SuccessDataResult<Announcement>(result, AnnouncementMessages.getAnnouncementByIdSuccess);
    }
}
