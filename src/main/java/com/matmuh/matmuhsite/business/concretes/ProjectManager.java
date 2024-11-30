package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.ProjectMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ProjectDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.Project;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectManager implements ProjectService {

    private final ProjectDao projectDao;

    private final ImageService imageService;

    private final UserService userService;


    public ProjectManager(ProjectDao projectDao, ImageService imageService, UserService userService) {
        this.projectDao = projectDao;
        this.imageService = imageService;
        this.userService = userService;
    }

    @Override
    public Result addProject(Project project, MultipartFile coverImage) {
        if (project.getName().isEmpty()){
            return new ErrorResult(ProjectMessages.nameCanotBeNull, HttpStatus.BAD_REQUEST);
        }
        if (project.getDescription().isEmpty()){
            return new ErrorResult(ProjectMessages.descriptionCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        Image projectImage = null;

        if(coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            projectImage = (Image) imageResult.getData();
        }

        var authenticatedUserResult = userService.getAuthenticatedUser();
        if (!authenticatedUserResult.isSuccess()){
            return authenticatedUserResult;
        }

        project.setPublisher(authenticatedUserResult.getData());
        project.setCoverImage(projectImage);
        project.setDate(LocalDateTime.now());

        projectDao.save(project);
        return new SuccessResult(ProjectMessages.projectAddSuccess, HttpStatus.CREATED);

    }
    @Override
    public Result updateProject(Project project, MultipartFile coverImage) {
        var result = projectDao.findById(project.getId());
        if (result.isEmpty()){
            return new ErrorResult(ProjectMessages.projectNotFound, HttpStatus.NOT_FOUND);
        }

        if (coverImage != null){
            var imageResult = imageService.addImage(coverImage);
            if (!imageResult.isSuccess()){
                return imageResult;
            }

            project.setCoverImage(imageResult.getData());
        }else {
            project.setCoverImage(result.get().getCoverImage());
        }

        var authenticatedUserResult = userService.getAuthenticatedUser();
        if (!authenticatedUserResult.isSuccess()){
            return authenticatedUserResult;
        }

        project.setPublisher(authenticatedUserResult.getData());
        projectDao.save(project);

        return new SuccessResult(ProjectMessages.projectUpdateSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<List<Project>> getProjects(Optional<Integer> numberOfProjects) {

        List<Project> result = new ArrayList<>();
        if(numberOfProjects.isPresent()) {
            result = projectDao.findAll(PageRequest.of(0, numberOfProjects.get())).toList();
        }
        else {
            result = projectDao.findAll();
        }

        if (result.isEmpty()){
            return new ErrorDataResult<>(ProjectMessages.projectsNotFound, HttpStatus.NOT_FOUND);
        }


        return new SuccessDataResult<List<Project>>(result, ProjectMessages.getProjectsSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Project> getProjectById(UUID id) {
        var result = projectDao.findById(id);

        if(result.isEmpty()){
            return new ErrorDataResult<>(ProjectMessages.projectNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<Project>(result.get(), ProjectMessages.getProjectSuccess, HttpStatus.OK);
    }


    @Override
    public Result deleteProject(UUID id) {

        var result = projectDao.findById(id);

        if (result.isEmpty()){
            return new ErrorResult(ProjectMessages.projectNotFound, HttpStatus.NOT_FOUND);
        }
        projectDao.delete(result.get());
        return new SuccessResult(ProjectMessages.deleteProjectsuccess, HttpStatus.OK);

    }

}
