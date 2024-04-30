package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseAnnouncementDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AnnouncementService {
    Result addAnnouncement(RequestAnnouncementDto announcementDto, MultipartFile image);

    Result updateAnnouncement(RequestAnnouncementDto requestAnnouncementDto);

    DataResult<List<ResponseAnnouncementDto>> getAnnouncements(Optional<Integer> numberOfAnnouncements);

    DataResult<Announcement> getAnnouncementById(int id);

    Result deleteAnnouncement(int id);


}
