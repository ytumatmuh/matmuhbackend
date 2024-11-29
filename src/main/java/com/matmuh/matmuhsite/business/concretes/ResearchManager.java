package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.business.constants.ResearchMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ResearchDao;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.entities.dtos.RequestResearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResearchManager implements ResearchService  {

    private final ResearchDao researchDao;


    public ResearchManager(ResearchDao researchDao) {
        this.researchDao = researchDao;
    }

    @Override
    public Result addResearch(Research research) {
        if (research.getTitle().isEmpty()) {
            return new ErrorResult(ResearchMessages.titleCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if (research.getDescription().isEmpty()) {
        return new ErrorResult(ResearchMessages.descriptionCanotBeNull, HttpStatus.BAD_REQUEST);
        }


        researchDao.save(research);
        return new SuccessResult(ResearchMessages.researchAddSuccess, HttpStatus.CREATED);
    }



    @Override
    public Result updateResearch(Research research) {

        var result = researchDao.findById(research.getId());
        if (result.isEmpty()){
            return new ErrorResult(ResearchMessages.researchNotFoundById, HttpStatus.NOT_FOUND);
        }

        researchDao.save(result.get());
        return new SuccessResult(ResearchMessages.researchAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public DataResult<List<Research>> getResearches(Optional<Integer> numberOfResearches) {
        List<Research> result = new ArrayList<>();

        if (numberOfResearches.isPresent()) {
            result = researchDao.findAll(PageRequest.of(0, numberOfResearches.get())).toList();
        } else {
            result = researchDao.findAll();
        }

        if(result == null){
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
        return new SuccessResult(ResearchMessages.researchAddSuccess, HttpStatus.CREATED);
    }

}
