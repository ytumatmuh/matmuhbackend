package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {

    DataResult<File> addFile(MultipartFile file);

    DataResult<File> getFileById(UUID id);

    DataResult<File> getFileByUrl(String url);

    Result deleteFileById(UUID id);


}
