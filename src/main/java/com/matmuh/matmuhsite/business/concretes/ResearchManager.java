package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.business.constants.ResearchMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.ResearchDao;
import com.matmuh.matmuhsite.entities.Research;
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
    public Result addResearch(Research research) {
        researchDao.save(research);
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

        return new SuccessDataResult<Research>(result, ResearchMessages.getResearchSuccess);
    }
}
