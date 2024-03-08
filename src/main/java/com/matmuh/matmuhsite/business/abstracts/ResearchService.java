package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;

import java.util.List;

public interface ResearchService {

    Result addResearch(Research research);

    DataResult<List<Research>> getResearchs();

    DataResult<Research> getResearchById(int id);


}
