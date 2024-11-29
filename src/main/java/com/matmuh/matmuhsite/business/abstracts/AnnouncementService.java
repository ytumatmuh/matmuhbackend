package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseAnnouncementDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnouncementService {
    Result addAnnouncement(Announcement announcement, Optional<MultipartFile> image);

    Result updateAnnouncement(Announcement announcement);

    DataResult<List<Announcement>> getAnnouncements(Optional<Integer> numberOfAnnouncements);

    DataResult<Announcement> getAnnouncementById(UUID id);

    Result deleteAnnouncement(UUID id);


}
