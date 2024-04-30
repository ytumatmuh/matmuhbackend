package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseProjectDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    Result addProject(RequestProjectDto requestProjectDto, MultipartFile image);

    Result updateProject(RequestProjectDto requestProjectDto);

    DataResult <List<ResponseProjectDto>> getProjects(Optional<Integer> numberOfProjects);

    DataResult<Project> getProjectById(int id);

    Result deleteProject(int id);


}
