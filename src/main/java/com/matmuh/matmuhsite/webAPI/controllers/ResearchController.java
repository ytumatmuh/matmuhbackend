package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.entities.dtos.RequestResearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/researches")
public class ResearchController {

    private final ResearchService researchService;

    @GetMapping(value = {"/getResearches", "/getResearches/{numberOfResearches}"})
    public DataResult<List<Research>> getResearches(@PathVariable Optional<Integer> numberOfResearches) {
        return researchService.getResearches(numberOfResearches);
    }

    @PostMapping("/addResearch")
    public Result addResearch(@RequestBody RequestResearchDto research) {
        return researchService.addResearch(research);
    }



}
