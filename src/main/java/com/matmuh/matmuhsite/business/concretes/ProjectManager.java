package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.business.constants.ProjectMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ProjectDao;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
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
    public Result addProject(RequestProjectDto requestProjectDto) {

        if (requestProjectDto.getName() == null){
            return new ErrorResult(ProjectMessages.nameCanotBeNull);
        }
        if (requestProjectDto.getDescription() == null){
            return new ErrorResult(ProjectMessages.descriptionCanotBeNull);
        }
        if (requestProjectDto.getDate() == null){
            return new ErrorResult(ProjectMessages.dateCanotBeNull);
        }
        if (requestProjectDto.getContext() == null){
            return new ErrorResult(ProjectMessages.contextCanotBeNull);
        }

        var projectToSave = Project.builder()
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .date(requestProjectDto.getDate())
                .context(requestProjectDto.getContext())
                .build();

        projectDao.save(projectToSave);
        return new SuccessResult(ProjectMessages.projectAddSuccess);

    }
    @Override
    public Result updateProject(RequestProjectDto requestProjectDto) {

            var result = getProjectById(requestProjectDto.getId());

            if (!result.isSuccess()){
                return result;
            }

            var projectToUpdate = result.getData();

            projectToUpdate.setContext(requestProjectDto.getContext()==null?projectToUpdate.getContext():requestProjectDto.getContext());
            projectToUpdate.setDate(requestProjectDto.getDate()==null?projectToUpdate.getDate():requestProjectDto.getDate());
            projectToUpdate.setDescription(requestProjectDto.getDescription()==null?projectToUpdate.getDescription():requestProjectDto.getDescription());
            projectToUpdate.setName(requestProjectDto.getName()==null?projectToUpdate.getName():requestProjectDto.getName());
            
            projectDao.save(projectToUpdate);
            return new SuccessResult(ProjectMessages.projectAddSuccess);
    }

    @Override
    public DataResult<List<Project>> getProjects() {
        var result = projectDao.findAll();

        return new SuccessDataResult<List<Project>>(result, ProjectMessages.getProjectsSuccess);
    }

    @Override
    public DataResult<Project> getProjectById(int id) {
        var result = projectDao.findById(id);

        if(result == null){
            return new ErrorDataResult<>(ProjectMessages.getProjectError);
        }

        return new SuccessDataResult<Project>(result, ProjectMessages.getProjectSuccess);
    }
    //dont repeat yourself chain
    @Override
    public Result deleteProject(int id) {

        var result = getProjectById(id);

        if (!result.isSuccess()){
            return result;
        }

        projectDao.delete(result.getData());
        return new SuccessResult(ProjectMessages.deleteProjectsuccess);

    }

}
