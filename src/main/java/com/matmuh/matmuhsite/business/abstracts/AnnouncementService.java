package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;

import java.util.List;

public interface AnnouncementService {
    Result addAnnouncement(RequestAnnouncementDto announcementDto);

    Result updateAnnouncement(RequestAnnouncementDto requestAnnouncementDto);

    DataResult<List<Announcement>> getAnnouncements();

    DataResult<Announcement> getAnnouncementById(int id);

    Result deleteAnnouncement(int id);


}
