package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseProjectDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectService {
    Result addProject(Project project, Optional<MultipartFile> image);

    Result updateProject(Project requestProjectDto);

    DataResult <List<Project>> getProjects(Optional<Integer> numberOfProjects);

    DataResult<Project> getProjectById(UUID id);

    Result deleteProject(UUID id);


}
