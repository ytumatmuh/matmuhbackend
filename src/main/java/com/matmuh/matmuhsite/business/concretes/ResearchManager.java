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
public class ResearchManager implements ResearchService {


    private final ResearchDao researchDao;

    @Autowired
    public ResearchManager(ResearchDao researchDao) {
        this.researchDao = researchDao;
    }



    @Override
    public Result addResearch(RequestResearchDto requestResearchDto) {
        if(requestResearchDto.getTitle() == null){
            return new ErrorResult(ResearchMessages.titleCanNotBeNull);
        }
        if(requestResearchDto.getDescription() == null){
            return new ErrorResult(ResearchMessages.descriptionCanNotBeNull);
        }
        if (requestResearchDto.getContext() == null){
            return new ErrorResult(ResearchMessages.contextCanNotBeNull);
        }

        var researchToSave = Research.builder()
                .title(requestResearchDto.getTitle())
                .description(requestResearchDto.getDescription())
                .context(requestResearchDto.getContext())
                .build();

        researchDao.save(researchToSave);
        return new SuccessResult(ResearchMessages.researchAddSuccess);
    }

    @Override
    public Result updateResearch(RequestResearchDto requestResearchDto) {
        var result = researchDao.findById(requestResearchDto.getId());

        if(result == null){
            return new ErrorResult(ResearchMessages.researchNotFound);
        }
        if(requestResearchDto.getTitle() == null){
            return new ErrorResult(ResearchMessages.titleCanNotBeNull);
        }
        if(requestResearchDto.getDescription() == null){
            return new ErrorResult(ResearchMessages.descriptionCanNotBeNull);
        }
        if (requestResearchDto.getContext() == null){
            return new ErrorResult(ResearchMessages.contextCanNotBeNull);
        }

        result.setTitle(requestResearchDto.getTitle().isEmpty() ? result.getTitle() : requestResearchDto.getTitle());
        result.setDescription(requestResearchDto.getDescription().isEmpty() ? result.getDescription() : requestResearchDto.getDescription());
        result.setContext(requestResearchDto.getContext().isEmpty() ? result.getContext() : requestResearchDto.getContext());
        researchDao.save(result);

        return new SuccessResult(ResearchMessages.researchAddSuccess);
    }

    @Override
    public DataResult<List<Research>> getResearchs() {
        var result = researchDao.findAll();

        if (result.isEmpty()){
            return new ErrorDataResult<List<Research>>(ResearchMessages.researchesNotFound);
        }

        return new SuccessDataResult<List<Research>>(result, ResearchMessages.getResearchsSuccess);
    }

    @Override
    public DataResult<Research> getResearchById(int id) {
        var result = researchDao.findById(id);

        if (result == null){
            return new ErrorDataResult<Research>(ResearchMessages.researchNotFound);
        }

        return new SuccessDataResult<Research>(result, ResearchMessages.getResearchSuccess);
    }

    @Override
    public Result deleteResearch(int id) {

        var result = researchDao.findById(id);

        if (result == null){
            return new ErrorResult(ResearchMessages.researchNotFound);
        }

        this.researchDao.deleteById(id);

        return new SuccessResult(ResearchMessages.researchDeleteSuccess);
    }

}
