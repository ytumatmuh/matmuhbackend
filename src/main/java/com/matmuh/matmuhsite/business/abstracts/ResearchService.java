package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResearchService {

    Result addResearch(Research research, MultipartFile coverImage);

    Result updateResearch(Research research, MultipartFile coverImage);

    DataResult<List<Research>> getResearches(Optional<Integer> numberOfResearchs);

    DataResult<Research> getResearchById(UUID id);

    Result deleteResearch(UUID id);


}
