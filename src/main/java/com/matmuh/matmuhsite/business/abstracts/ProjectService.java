package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectService {
    Result addProject(Project project, MultipartFile coverImage);

    Result updateProject(Project requestProjectDto, MultipartFile coverImage);

    DataResult <List<Project>> getProjects(Optional<Integer> numberOfProjects);

    DataResult<Project> getProjectById(UUID id);

    Result deleteProject(UUID id);


}
