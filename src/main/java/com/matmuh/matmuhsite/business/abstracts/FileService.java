package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    DataResult<File> addFile(MultipartFile file);

    DataResult<File> getFileById(int id);

    DataResult<File> getFileByUrl(String url);


}
