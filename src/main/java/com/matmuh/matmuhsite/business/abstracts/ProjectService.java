package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Project;

import java.util.List;

public interface ProjectService {
    Result addProject(Project project);

    DataResult <List<Project>> getProjects();
    DataResult<Project> getProjectById(int id);
}
