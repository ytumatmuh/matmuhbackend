package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ImageService;
import com.matmuh.matmuhsite.business.abstracts.ProjectService;
import com.matmuh.matmuhsite.business.constants.ProjectMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.ProjectDao;
import com.matmuh.matmuhsite.entities.Image;
import com.matmuh.matmuhsite.entities.Project;
import com.matmuh.matmuhsite.entities.dtos.RequestProjectDto;
import com.matmuh.matmuhsite.entities.dtos.ResponseProjectDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectManager implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ImageService imageService;

    @Value("${api.url}")
    private String API_URL;

    @Override
    public Result addProject(RequestProjectDto requestProjectDto, MultipartFile image) {

        if (requestProjectDto.getName() == null){
            return new ErrorResult(ProjectMessages.nameCanotBeNull);
        }
        if (requestProjectDto.getDescription() == null){
            return new ErrorResult(ProjectMessages.descriptionCanotBeNull);
        }
        if (requestProjectDto.getDate() == null){
            return new ErrorResult(ProjectMessages.dateCanotBeNull);
        }

        Image projectImage = null;

        if(image != null){
            var imageResult = imageService.addImage(image);
            if (!imageResult.isSuccess()){
                return new ErrorResult(imageResult.getMessage());
            }

            projectImage = imageResult.getData();
        }

        var projectToSave = Project.builder()
                .name(requestProjectDto.getName())
                .description(requestProjectDto.getDescription())
                .date(requestProjectDto.getDate())
                .image(projectImage)
                .build();

        projectDao.save(projectToSave);
        return new SuccessResult(ProjectMessages.projectAddSuccess);

    }
    @Override
    public Result updateProject(RequestProjectDto requestProjectDto) {

//            var result = getProjectById(requestProjectDto.getId());
//
//            if (!result.isSuccess()){
//                return result;
//            }
//
//            var projectToUpdate = result.getData();
//
//            projectToUpdate.setContext(requestProjectDto.getContext()==null?projectToUpdate.getContext():requestProjectDto.getContext());
//            projectToUpdate.setDate(requestProjectDto.getDate()==null?projectToUpdate.getDate():requestProjectDto.getDate());
//            projectToUpdate.setDescription(requestProjectDto.getDescription()==null?projectToUpdate.getDescription():requestProjectDto.getDescription());
//            projectToUpdate.setName(requestProjectDto.getName()==null?projectToUpdate.getName():requestProjectDto.getName());

//            projectDao.save(projectToUpdate);

            var project = projectDao.findById(requestProjectDto.getId());

            if (project == null){
                return new ErrorResult(ProjectMessages.projectNotFound);
            }
            if (requestProjectDto.getName() == null){
                return new ErrorResult(ProjectMessages.nameCanotBeNull);
            }
            if (requestProjectDto.getDescription() == null){
                return new ErrorResult(ProjectMessages.descriptionCanotBeNull);
            }


            project.setName(requestProjectDto.getName().isEmpty() ? project.getName() : requestProjectDto.getName());
            project.setDescription(requestProjectDto.getDescription().isEmpty() ? project.getDescription() : requestProjectDto.getDescription());
            //project.setContext(requestProjectDto.getContext().isEmpty() ? project.getContext() : requestProjectDto.getContext());

            projectDao.save(project);

            return new SuccessResult(ProjectMessages.projectAddSuccess);
    }

    @Override
    public DataResult<List<ResponseProjectDto>> getProjects(Optional<Integer> numberOfProjects) {

        List<Project> result = new ArrayList<>();
        if(numberOfProjects.isPresent()) {
            result = projectDao.findAll(PageRequest.of(0, numberOfProjects.get())).toList();
        }
        else {

            result = projectDao.findAll();
        }

        if (result == null){
            return new ErrorDataResult<>(ProjectMessages.PorjectsNotFound);
        }

        var projectResult = result.stream().map(project -> ResponseProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .date(project.getDate())
                .imageUrl(project.getImage()==null? null : API_URL+"/api/images/getImageByUrl/"+project.getImage().getUrl())
                .build())
                .toList();

        return new SuccessDataResult<List<ResponseProjectDto>>(projectResult, ProjectMessages.getProjectsSuccess);
    }

    @Override
    public DataResult<Project> getProjectById(int id) {
        var result = projectDao.findById(id);

        if(result == null){
            return new ErrorDataResult<>(ProjectMessages.projectNotFound);
        }

        return new SuccessDataResult<Project>(result, ProjectMessages.getProjectSuccess);
    }
    //dont repeat yourself chain
    @Override
    public Result deleteProject(int id) {

//        var result = getProjectById(id);
//
//        if (!result.isSuccess()){
//            return result;
//        }

        var result = projectDao.findById(id);

        if (result == null){
            return new ErrorResult(ProjectMessages.projectNotFound);
        }
        projectDao.delete(result);
        return new SuccessResult(ProjectMessages.deleteProjectsuccess);

    }

}
