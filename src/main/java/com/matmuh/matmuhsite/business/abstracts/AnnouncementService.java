package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;

import java.util.List;

public interface AnnouncementService {
    Result addAnnouncement(Announcement announcement);

    DataResult<List<Announcement>> getAnnouncements();

    DataResult<Announcement> getAnnouncementById(int id);


}
