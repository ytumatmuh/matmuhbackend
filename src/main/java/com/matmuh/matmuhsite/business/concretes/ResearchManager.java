package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.business.constants.ResearchMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ResearchDao;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.entities.dtos.RequestResearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public record ResearchManager (ResearchDao researchDao) implements ResearchService  {


    @Override
    public Result addResearch(RequestResearchDto requestResearchDto) {
        if (requestResearchDto.getTitle() == null) {
            return new ErrorResult(ResearchMessages.titleCanotBeNull);
        }

        if (requestResearchDto.getDescription() == null) {
        return new ErrorResult(ResearchMessages.descriptionCanotBeNull);
        }

        if (requestResearchDto.getContext() == null) {
            return new ErrorResult(ResearchMessages.contextCanotBeNull);
        }

        Research research = Research.builder()
                .title(requestResearchDto.getTitle())
                .description(requestResearchDto.getDescription())
                .context(requestResearchDto.getContext())
                .build();
        this.researchDao.save(research);
        return new SuccessResult(ResearchMessages.researchAddSuccess);
    }



    @Override
    public Result updateResearch(RequestResearchDto requestResearchDto) {
        var result = researchDao.findById(requestResearchDto.getId());


        result.setContext(requestResearchDto.getContext()==null?result.getContext():requestResearchDto.getContext());
        result.setDescription(requestResearchDto.getDescription()==null?result.getDescription():requestResearchDto.getDescription());
        result.setTitle(requestResearchDto.getTitle()==null?result.getTitle():requestResearchDto.getTitle());

        this.researchDao.save(result);
        return new SuccessResult(ResearchMessages.researchAddSuccess);
    }

    @Override
    public DataResult<List<Research>> getResearchs() {
        var result = researchDao.findAll();

        return new SuccessDataResult<List<Research>>(result, ResearchMessages.getResearchsSuccess);
    }

    @Override
    public DataResult<Research> getResearchById(int id) {
        var result = researchDao.findById(id);

        if (result == null) {
            return new SuccessDataResult<>(ResearchMessages.researchNotFoundById);
        }

        return new SuccessDataResult<Research>(result, ResearchMessages.getResearchSuccess);
    }

    @Override
    public Result deleteResearch(int id) {
        var result = researchDao.findById(id);

        if (result == null) {
            return new ErrorResult(ResearchMessages.researchNotFoundById);
        }

        researchDao.delete(result);
        return new SuccessResult(ResearchMessages.researchAddSuccess);
    }

}
