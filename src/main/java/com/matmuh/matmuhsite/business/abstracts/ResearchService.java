package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResearchService {

    Result addResearch(Research research);

    Result updateResearch(Research research);

    DataResult<List<Research>> getResearches(Optional<Integer> numberOfResearchs);

    DataResult<Research> getResearchById(UUID id);

    Result deleteResearch(UUID id);


}
