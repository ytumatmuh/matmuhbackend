package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.OfferingSummaryDto;
import com.matmuh.matmuhsite.core.helpers.InstructorNames;
import com.matmuh.matmuhsite.core.helpers.StorageUrlResolver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LectureNoteMapper {

    @Autowired
    protected StorageUrlResolver storageUrlResolver;

    @Named("addDomainToUrl")
    protected String generateFullUrl(String key) {
        return storageUrlResolver.urlFor(key);
    }

    @Mapping(target = "file", expression = "java(toFileEntity(fileDto))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lecture", ignore = true)
    public abstract LectureNote toLectureNote(LectureNoteCreateRequestDto lectureNoteCreateRequestDto, FileDto fileDto);

    @Mapping(target = "fileUrl", expression = "java(generateFullUrl(file.getFileUrl()))")
    public abstract FileDto toFileDto(File file);

    public abstract File toFileEntity(FileDto fileDto);

    @Mapping(target = "offering", expression = "java(offeringOf(lectureNote))")
    public abstract LectureNoteDto toLectureNoteDto(LectureNote lectureNote);


    protected OfferingSummaryDto offeringOf(LectureNote lectureNote) {
        if (lectureNote == null || lectureNote.getLectureOffering() == null) {
            return null;
        }

        var offering = lectureNote.getLectureOffering();
        return new OfferingSummaryDto(
                offering.getId(),
                offering.getAcademicYear(),
                offering.getSemester(),
                offering.getGroupNumber(),
                InstructorNames.of(offering));
    }


    public abstract LectureNote toLectureNote(LectureNoteDto lectureNoteDto);

    public abstract List<LectureNoteDto> toLectureNoteDtos(List<LectureNote> lectureNotes);

    @Mapping(source = "file", target = "file")
    @Mapping(source = "lecture", target = "lecture")
    @Mapping(target = "offering", expression = "java(offeringOf(lectureNote))")
    public abstract LectureNoteWithLectureDto toLectureNoteWithLectureDto(LectureNote lectureNote);

}
