package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureOfferingImportService;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.ImportOfferingsRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.ImportResultDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LectureOfferingImportManager implements LectureOfferingImportService {

    private final Logger logger = LoggerFactory.getLogger(LectureOfferingImportManager.class);

    private final LectureOfferingImportRowWriter rowWriter;
    private final MessageResolver messageResolver;

    public LectureOfferingImportManager(LectureOfferingImportRowWriter rowWriter, MessageResolver messageResolver) {
        this.rowWriter = rowWriter;
        this.messageResolver = messageResolver;
    }


    @Override
    public ImportResultDto importOfferings(ImportOfferingsRequestDto request) {
        var rows = request.getRows();
        logger.info("Importing {} lecture offerings", rows.size());

        var results = new ArrayList<ImportResultDto.RowResult>(rows.size());
        int created = 0;
        int updated = 0;
        int failed = 0;

        for (int index = 0; index < rows.size(); index++) {
            var row = rows.get(index);
            var key = new ImportResultDto.RowKey(row.getLectureCode(), row.getAcademicYear(),
                    row.getSemester(), row.getGroupNumber());
            var result = new ImportResultDto.RowResult();
            result.setIndex(index);
            result.setKey(key);

            try {
                var outcome = rowWriter.write(row);
                result.setOfferingId(outcome.offeringId());
                result.setStatus(outcome.created()
                        ? ImportResultDto.Status.CREATED
                        : ImportResultDto.Status.UPDATED);
                if (outcome.created()) {
                    created++;
                } else {
                    updated++;
                }
            } catch (Exception exception) {
                failed++;
                result.setStatus(ImportResultDto.Status.FAILED);
                result.setError(messageResolver.resolve(exception.getMessage()));
                logger.warn("Import row {} failed for {}: {}", index, key, exception.getMessage());
            }

            results.add(result);
        }

        logger.info("Import finished: {} created, {} updated, {} failed", created, updated, failed);

        return new ImportResultDto(rows.size(), created, updated, failed, results);
    }
}
