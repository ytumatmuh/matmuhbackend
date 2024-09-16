package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.FileMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.FileDao;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.Base64;

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

        if (file.getContentType().equals("application/pdf") && file.getContentType().equals("application/vnd.ms-excel")){
            return new ErrorDataResult<>(FileMessages.fileTypeNotSupported);
        }

        var userResult = userService.getAuthenticatedUser();

        if (!userResult.isSuccess()){
          return new ErrorDataResult<>(null, userResult.getMessage());
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

            return new SuccessDataResult<>(fileToSave, FileMessages.pdfAddSuccess);
        } catch (Exception e) {
            return new ErrorDataResult<>(e.getMessage());
        }


    }

    @Override
    public DataResult<File> getFileById(int id) {
        var pdf = fileDao.findById(id);

        if (!pdf.isPresent()){
            return new ErrorDataResult<>(FileMessages.pdfNotFound);
        }

        return new SuccessDataResult<>(pdf.get(), FileMessages.pdfGetSuccess);

    }

    @Override
    public DataResult<File> getFileByUrl(String url) {
        var file = fileDao.findFileByUrl(url);

        if (!file.isPresent()){
            return new ErrorDataResult<>(FileMessages.pdfNotFound);
        }

        return new SuccessDataResult<>(file.get(), FileMessages.pdfGetSuccess);
    }


    private String generateUrl() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
