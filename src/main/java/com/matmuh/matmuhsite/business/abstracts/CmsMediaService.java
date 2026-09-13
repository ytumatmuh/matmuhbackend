package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.response.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface CmsMediaService {

    UploadResponseDto upload(MultipartFile file, boolean publicAccess);

}
