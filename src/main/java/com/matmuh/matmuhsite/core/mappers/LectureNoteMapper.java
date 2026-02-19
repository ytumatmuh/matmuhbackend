package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.LectureNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LectureNoteMapper {

    @Value("${app.storage.domain}")
   protected String storageDomain;

    @Named("addDomainToUrl")
    protected String generateFullUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        if (key.startsWith("http")) {
            return key;
        }
        return storageDomain + "/" + key;
    }

    @Mapping(target = "file", expression = "java(toFileEntity(fileDto))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lecture", ignore = true)
    public abstract LectureNote toLectureNote(LectureNoteCreateRequestDto lectureNoteCreateRequestDto, FileDto fileDto);

    @Mapping(target = "fileUrl", expression = "java(generateFullUrl(file.getFileUrl()))")
    public abstract FileDto toFileDto(File file);

    public abstract File toFileEntity(FileDto fileDto);

    public abstract LectureNoteDto toLectureNoteDto(LectureNote lectureNote);


    public abstract LectureNote toLectureNote(LectureNoteDto lectureNoteDto);

    public abstract List<LectureNoteDto> toLectureNoteDtos(List<LectureNote> lectureNotes);

    @Mapping(source = "file", target = "file")
    @Mapping(source = "lecture", target = "lecture")
    public abstract LectureNoteWithLectureDto toLectureNoteWithLectureDto(LectureNote lectureNote);

}
