package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.FileMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.FileDao;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileManager implements FileService {

    private final FileDao fileDao;

    private final UserService userService;


    public FileManager(FileDao fileDao, UserService userService) {
        this.fileDao = fileDao;
        this.userService = userService;
    }


    @Override
    public DataResult<File> addFile(MultipartFile file) {

        if (file == null){
            return new ErrorDataResult<>(FileMessages.fileCanNotBeNull, HttpStatus.BAD_REQUEST);
        }

        if (file.getContentType().equals("application/pdf") && file.getContentType().equals("application/vnd.ms-excel")){
            return new ErrorDataResult<>(FileMessages.fileTypeNotSupported, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var userResult = userService.getAuthenticatedUser();

        if (!userResult.isSuccess()){
         return new ErrorDataResult<>(userResult.getMessage(), userResult.getHttpStatus());
        }

        try {
            File fileToSave = File.builder()
                    .name(file.getOriginalFilename())
                    .data(file.getBytes())
                    .type(file.getContentType())
                    .url(generateUrl())
                    .createdBy(userResult.getData())
                    .build();

            fileDao.save(fileToSave);

            return new SuccessDataResult<>(fileToSave, FileMessages.fileAddSuccess, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ErrorDataResult<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @Override
    public DataResult<File> getFileById(UUID id) {
        var pdf = fileDao.findById(id);

        if (!pdf.isPresent()){
            return new ErrorDataResult<>(FileMessages.fileNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<>(pdf.get(), FileMessages.fileGetSuccess, HttpStatus.OK);

    }

    @Override
    public DataResult<File> getFileByUrl(String url) {
        var file = fileDao.findFileByUrl(url);

        if (!file.isPresent()){
            return new ErrorDataResult<>(FileMessages.fileNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<>(file.get(), FileMessages.fileGetSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteFileById(UUID id) {
        var file = fileDao.findById(id);

        if (!file.isPresent()){
            return new ErrorResult(FileMessages.fileNotFound, HttpStatus.NOT_FOUND);
        }

        fileDao.delete(file.get());

        return new SuccessResult(FileMessages.fileDeleteSuccess, HttpStatus.OK);
    }


    private String generateUrl() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
