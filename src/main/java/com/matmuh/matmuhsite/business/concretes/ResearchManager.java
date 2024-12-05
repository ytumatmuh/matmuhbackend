package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.ResearchMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ResearchDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.Research;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResearchManager implements ResearchService  {

    private final ResearchDao researchDao;

    private final ImageService imageService;

    private final UserService userService;


    public ResearchManager(ResearchDao researchDao, ImageService imageService, UserService userService) {
        this.researchDao = researchDao;
        this.imageService = imageService;
        this.userService = userService;
    }

    @Override
    public Result addResearch(Research research, MultipartFile coverImage) {

        if (research.getTitle() == null){
            return new ErrorResult(ResearchMessages.titleCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if (research.getDescription() == null){
            return new ErrorResult(ResearchMessages.descriptionCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        Image researchImage = null;

        if(coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            researchImage =imageResult.getData();
        }

        research.setCreatedAt(LocalDateTime.now());
        research.setCoverImage(researchImage);

        researchDao.save(research);

        return new SuccessResult(ResearchMessages.researchAddSuccess, HttpStatus.CREATED);

    }



    @Override
    public Result updateResearch(Research research, MultipartFile coverImage) {

        var result = researchDao.findById(research.getId());
        if (result.isEmpty()){
            return new ErrorResult(ResearchMessages.researchNotFoundById, HttpStatus.NOT_FOUND);
        }

        if (coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }
            research.setCoverImage(imageResult.getData());

        }else {
            research.setCoverImage(result.get().getCoverImage());
        }

        research.setCreatedAt(result.get().getCreatedAt());

        researchDao.save(research);
        return new SuccessResult(ResearchMessages.researchUpdateSuccess, HttpStatus.CREATED);
    }

    @Override
    public DataResult<List<Research>> getResearches(Optional<Integer> numberOfResearches) {
        List<Research> result = new ArrayList<>();

        if (numberOfResearches.isPresent()) {
            result = researchDao.findAll(PageRequest.of(0, numberOfResearches.get())).toList();
        } else {
            result = researchDao.findAll();
        }

        if(result.isEmpty()){
            return new ErrorDataResult<>(ResearchMessages.researchNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<List<Research>>(result, ResearchMessages.getResearchsSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Research> getResearchById(UUID id) {
        var result = researchDao.findById(id);

        if (result.isEmpty()) {
            return new SuccessDataResult<>(ResearchMessages.researchNotFoundById, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<Research>(result.get(), ResearchMessages.getResearchSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteResearch(UUID id) {
        var result = researchDao.findById(id);

        if (result.isEmpty()) {
            return new ErrorResult(ResearchMessages.researchNotFoundById, HttpStatus.NOT_FOUND);
        }

        researchDao.delete(result.get());
        return new SuccessResult(ResearchMessages.researchDeleteSuccess, HttpStatus.OK);
    }

}
