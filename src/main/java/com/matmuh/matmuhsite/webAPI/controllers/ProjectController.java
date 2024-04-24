package com.matmuh.matmuhsite.webAPI.controllers;


import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/projects")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @PostMapping("/add")
    public Result addProject(@RequestBody RequestProjectDto requestProjectDto) {
        return projectService.addProject(requestProjectDto);
    }
    @PostMapping("/update")
    public Result updateProject(@RequestBody RequestProjectDto requestProjectDto) {
        return projectService.updateProject(requestProjectDto);
    }
    @GetMapping("/getAll")
    public Result getProjects(){
        return projectService.getProjects();
    }
    @GetMapping("/getById")
    public DataResult<Project> getProjectById(@RequestParam int id){
        return projectService.getProjectById(id);
    }
    @PostMapping("/delete")
    public Result deleteProject(@RequestParam int id){
        return projectService.deleteProject(id);
    }

}
