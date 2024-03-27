package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.business.constants.EventMessages;
import com.matmuh.matmuhsite.business.constants.ProjectMessages;
import com.matmuh.matmuhsite.business.constants.ResearchMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.ProjectDao;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.Research;
import com.matmuh.matmuhsite.entities.dtos.RequestPhotoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectManager implements ProjectService {
    private final ProjectDao projectDao;

    @Autowired
    public ProjectManager(ProjectDao projectDao){
        this.projectDao = projectDao;
    }

    @Override
    public Result addProject(Project project){
        projectDao.save(project);
        return new SuccessResult(ProjectMessages.projectAddSuccess);
    }

    @Override
    public Result addProject(RequestPhotoDto requestPhotoDto) {
        return null;
    }

    @Override
    public Result updateProject(RequestPhotoDto requestPhotoDto) {
        return null;
    }

    @Override
    public DataResult<List<Project>> getProjects() {
        var result = projectDao.findAll();

        return new SuccessDataResult<List<Project>>(result, ProjectMessages.getProjectsSuccess);
    }

    @Override
    public DataResult<Project> getProjectById(int id) {
        var result = projectDao.findById(id);

        return new SuccessDataResult<Project>(result, ProjectMessages.getProjectSuccess);
    }
}
