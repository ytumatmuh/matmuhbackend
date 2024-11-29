package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {

    DataResult<?> addFile(MultipartFile file);

    DataResult<File> getFileById(UUID id);

    DataResult<File> getFileByUrl(String url);


}
