package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestPhotoDto;

import java.util.List;

public interface ProjectService {
    Result addProject(RequestPhotoDto requestPhotoDto);

    Result updateProject(RequestPhotoDto requestPhotoDto);

    DataResult <List<Project>> getProjects();

    DataResult<Project> getProjectById(int id);

    Result deleteProject(int id);


}
