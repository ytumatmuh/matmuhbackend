package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.business.concretes.LectureManager;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LectureOfferingMapper.class})
public interface LectureMapper {

    LectureDto toDto(Lecture lecture);

    List<LectureDto> toDtoList(List<Lecture> lectureList);

    Lecture toEntity(LectureDto lectureDto);

    Lecture toEntity(CreateLectureRequestDto createLectureRequestDto);

    List<LectureDto> toLectureDtos(List<Lecture> lectures);

    LectureStatisticsDto toLectureStatisticsDto(Lecture lecture);

    LectureStatisticsDto toLectureStatisticsDto(LectureDto lectureDto);

}
