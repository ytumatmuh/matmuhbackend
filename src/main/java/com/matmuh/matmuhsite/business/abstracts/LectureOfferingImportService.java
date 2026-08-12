package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.ImportOfferingsRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.ImportResultDto;

public interface LectureOfferingImportService {

    ImportResultDto importOfferings(ImportOfferingsRequestDto request);
}
