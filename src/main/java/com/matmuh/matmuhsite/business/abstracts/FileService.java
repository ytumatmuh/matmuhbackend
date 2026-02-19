package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {

    FileDto uploadFile(MultipartFile file);

    void deleteFile(UUID fileId);

    File getReference(UUID id);

}
