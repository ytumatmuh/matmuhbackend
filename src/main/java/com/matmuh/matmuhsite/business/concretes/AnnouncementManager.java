package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AnnouncementService;
import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.constants.AnnouncementMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.AnnouncementLinkDao;
import com.matmuh.matmuhsite.entities.Announcement;
import com.matmuh.matmuhsite.entities.AnnouncementLink;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.dtos.RequestAnnouncementDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseAnnouncementDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AnnouncementManager implements AnnouncementService {

    private AnnouncementDao announcementDao;

    private ImageService imageService;

    private AnnouncementLinkDao announcementLinkDao;


    @Override
    public Result addAnnouncement(RequestAnnouncementDto announcementDto, MultipartFile image) {
        if (announcementDto.getTitle() == null){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull);
        }

        if(announcementDto.getContent() == null){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull);
        }

         Image announcementImage = null;

        if(image != null){
            var imageResult = imageService.addImage(image);
            if (!imageResult.isSuccess()){
                return new ErrorResult(imageResult.getMessage());
            }

            announcementImage = imageResult.getData();
        }


        var announcementToSave = Announcement.builder()
                .title(announcementDto.getTitle())
                .content(announcementDto.getContent())
                .publishDate(new Date())
                .image(announcementImage)
                .build();

        var announcementLinksToSave = announcementDto.getLinks().stream().map(link -> AnnouncementLink.builder()
                        .link(link)
                        .announcement(announcementToSave)
                        .build())
                .toList();

        announcementDao.save(announcementToSave);
        announcementLinkDao.saveAll(announcementLinksToSave);
        return new SuccessResult(AnnouncementMessages.announcementAddSuccess);
    }

    @Override
    public Result updateAnnouncement(RequestAnnouncementDto requestAnnouncementDto) {

        var announcement = announcementDao.findById(requestAnnouncementDto.getId());

        if (announcement == null){
            return new ErrorResult(AnnouncementMessages.announcementNotFound);
        }

        if(requestAnnouncementDto.getTitle() == null){
            return new ErrorResult(AnnouncementMessages.nameCanotBeNull);
        }

        if(requestAnnouncementDto.getContent() == null){
            return new ErrorResult(AnnouncementMessages.contentCanotBeNull);
        }

        announcement.setTitle(requestAnnouncementDto.getTitle().isEmpty() ? announcement.getTitle() : requestAnnouncementDto.getTitle());
        announcement.setContent(requestAnnouncementDto.getContent().isEmpty() ? announcement.getContent() : requestAnnouncementDto.getContent());

        announcementDao.save(announcement);

        return new SuccessResult(AnnouncementMessages.announcementUpdateSuccess);
    }

    @Override
    public DataResult<List<ResponseAnnouncementDto>> getAnnouncements(Optional<Integer> numberOfAnnouncements) {
            List<Announcement> result = new ArrayList<>();
        if (numberOfAnnouncements.isPresent()){
            result = announcementDao.findAll(PageRequest.of(0, numberOfAnnouncements.get())).toList();
        }else{
            result = announcementDao.findAll();
        }

        if (result == null){
            return new ErrorDataResult<>(AnnouncementMessages.annoucementsNotFound);
        }

        var announcementResult = result.stream().map(announcement -> ResponseAnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .publishDate(announcement.getPublishDate())
                .imageUrl(announcement.getImage()==null? null : "http://localhost:8080/api/images/getImageByUrl/"+announcement.getImage().getImageUrl())
                .links(announcement.getLinks())
                .build()).toList();

        return new SuccessDataResult<List<ResponseAnnouncementDto>>(announcementResult, AnnouncementMessages.getAnnouncementsSuccess);
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
