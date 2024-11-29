package com.matmuh.matmuhsite.webAPI.controllers;


import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/projects")
public class ProjectController {

    private final ProjectService projectService;


    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/addProject")
    public ResponseEntity<Result> addProject(
            @RequestPart("data") RequestProjectDto requestProjectDto,
            @RequestPart("image") Optional<MultipartFile> image) {

        Project projectToSave = Project.builder()
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .build();

        var addProjectResult = projectService.addProject(projectToSave, image);

        return ResponseEntity.status(addProjectResult.getHttpStatus()).body(addProjectResult);
    }


    @PostMapping("/update")
    public ResponseEntity<Result> updateProject(@RequestBody RequestProjectDto requestProjectDto) {

        Project projectToUpdate = Project.builder()
                .id(requestProjectDto.getId())
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .build();

        var updateProjectResult = projectService.updateProject(projectToUpdate);

        return ResponseEntity.status(updateProjectResult.getHttpStatus()).body(updateProjectResult);
    }


    @GetMapping(value = { "/getProjects/{numberOfProjects}", "/getProjects" })
    public ResponseEntity<DataResult<List<ResponseProjectDto>>> getProjects(@PathVariable Optional<Integer> numberOfProjects){

            var result = projectService.getProjects(numberOfProjects);
            if (!result.isSuccess()){
                return ResponseEntity.status(result.getHttpStatus()).body(
                        new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
                );
            }

            List<ResponseProjectDto> projectsToReturn = result.getData().stream().map(project -> {
                return ResponseProjectDto.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .imageUrl(project.getImage().getUrl())
                        .build();
            }).toList();

            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(projectsToReturn, result.getMessage(), result.getHttpStatus())
            );
    }


    @GetMapping("/getById/{id}")
    public ResponseEntity<DataResult<Project>> getProjectById(@PathVariable UUID id){
        var result = projectService.getProjectById(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Result> deleteProject(@PathVariable UUID id){
        var result = projectService.deleteProject(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

}
