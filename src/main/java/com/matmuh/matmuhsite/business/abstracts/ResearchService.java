package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.entities.dtos.RequestResearchDto;

import java.util.List;

public interface ResearchService {

    Result addResearch(RequestResearchDto requestResearchDto);

    Result updateResearch(RequestResearchDto requestResearchDto);

    DataResult<List<Research>> getResearchs();

    DataResult<Research> getResearchById(int id);

    Result deleteResearch(int id);


}
