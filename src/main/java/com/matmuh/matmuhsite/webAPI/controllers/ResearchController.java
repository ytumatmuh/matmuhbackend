package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ResearchService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.webAPI.dtos.RequestResearchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/researches")
public class ResearchController {

    private final ResearchService researchService;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @GetMapping(value = {"/getResearches", "/getResearches/{numberOfResearches}", "/getResearches/"})
    public ResponseEntity<DataResult<List<Research>>> getResearches(@PathVariable Optional<Integer> numberOfResearches) {
       var result = researchService.getResearches(numberOfResearches);

       return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @PostMapping("/addResearch")
    public ResponseEntity<Result> addResearch(@RequestBody RequestResearchDto researchDto) {

        Research research = Research.builder()
                .title(researchDto.getTitle())
                .description(researchDto.getDescription())
                .build();

        var result = researchService.addResearch(research);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @GetMapping("/getResearchById/{id}")
    public ResponseEntity<DataResult<Research>> getResearchById(@PathVariable UUID id) {
        var result = researchService.getResearchById(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @PutMapping("/updateResearchById/{id}")
    public ResponseEntity<Result> updateResearch(@PathVariable Optional<UUID> id, @RequestBody RequestResearchDto researchDto) {
        Research research = Research.builder()
                .id(id.get())
                .title(researchDto.getTitle())
                .description(researchDto.getDescription())
                .build();

        var result = researchService.updateResearch(research);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @DeleteMapping("/deleteResearchById/{id}")
    public ResponseEntity<Result> deleteResearch(@PathVariable UUID id) {
        var result = researchService.deleteResearch(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

}
