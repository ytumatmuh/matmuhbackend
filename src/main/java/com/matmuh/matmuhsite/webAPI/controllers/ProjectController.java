package com.matmuh.matmuhsite.webAPI.controllers;


import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.webAPI.dtos.projects.RequestProjectDto;
import com.matmuh.matmuhsite.webAPI.dtos.projects.ResponseProjectDto;
import com.matmuh.matmuhsite.webAPI.dtos.users.ResponseUserDto;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${api.url}")
    private String API_URL;

    @Value("${image.get.url}")
    private String IMAGE_GET_URL;


    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/addProject")
    public ResponseEntity<Result> addProject(
            @RequestPart("data") RequestProjectDto requestProjectDto,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        Project projectToSave = Project.builder()
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .build();

        var addProjectResult = projectService.addProject(projectToSave, coverImage);

        return ResponseEntity.status(addProjectResult.getHttpStatus()).body(addProjectResult);
    }


    @PutMapping("/updateProjectById/{id}")
    public ResponseEntity<Result> updateProject( @PathVariable UUID id,
                                                 @RequestPart("data") RequestProjectDto requestProjectDto,
                                                 @RequestPart(value = "coverImage", required = false) MultipartFile coverImage){

        Project projectToUpdate = Project.builder()
                .id(id)
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .build();

        var updateProjectResult = projectService.updateProject(projectToUpdate, coverImage);

        return ResponseEntity.status(updateProjectResult.getHttpStatus()).body(updateProjectResult);
    }


    @GetMapping(value = { "/getProjects/{numberOfProjects}", "/getProjects", "/getProjects/"})
    public ResponseEntity<DataResult<List<ResponseProjectDto>>> getProjects(@PathVariable Optional<Integer> numberOfProjects){

            var result = projectService.getProjects(numberOfProjects);
            if (!result.isSuccess()){
                return ResponseEntity.status(result.getHttpStatus()).body(
                        new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
                );
            }

            List<ResponseProjectDto> projectsToReturn = result.getData().stream().map(project -> {

                ResponseUserDto publisherToReturnDto = ResponseUserDto.builder()
                        .id(project.getPublisher().getId())
                        .email(project.getPublisher().getEmail())
                        .firstName(project.getPublisher().getFirstName())
                        .lastName(project.getPublisher().getLastName())
                        .username(project.getPublisher().getUsername())
                        .build();


                return ResponseProjectDto.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .date(project.getDate())
                        .coverImageUrl(project.getCoverImage() != null ? API_URL + IMAGE_GET_URL + project.getCoverImage().getUrl() : null)
                        .publisher(publisherToReturnDto)
                        .build();
            }).toList();

            return ResponseEntity.status(result.getHttpStatus()).body(
                    new SuccessDataResult<>(projectsToReturn, result.getMessage(), result.getHttpStatus())
            );
    }


    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<DataResult<ResponseProjectDto>> getProjectById(@PathVariable UUID id){
        var result = projectService.getProjectById(id);
        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(
                    new ErrorDataResult<>(result.getMessage(), result.getHttpStatus())
            );
        }

        ResponseUserDto publisherToReturnDto = ResponseUserDto.builder()
                .id(result.getData().getPublisher().getId())
                .email(result.getData().getPublisher().getEmail())
                .firstName(result.getData().getPublisher().getFirstName())
                .lastName(result.getData().getPublisher().getLastName())
                .username(result.getData().getPublisher().getUsername())
                .build();

        ResponseProjectDto projectToReturn = ResponseProjectDto.builder()
                .id(result.getData().getId())
                .name(result.getData().getName())
                .description(result.getData().getDescription())
                .date(result.getData().getDate())
                .coverImageUrl(result.getData().getCoverImage() != null ? API_URL + IMAGE_GET_URL + result.getData().getCoverImage().getUrl() : null)
                .publisher(publisherToReturnDto)
                .build();

        return ResponseEntity.status(result.getHttpStatus()).body(
                new SuccessDataResult<>(projectToReturn, result.getMessage(), result.getHttpStatus())
        );

    }


    @DeleteMapping("/deleteProjectById/{id}")
    public ResponseEntity<Result> deleteProject(@PathVariable UUID id){
        var result = projectService.deleteProject(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

}
