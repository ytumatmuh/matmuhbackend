package com.matmuh.matmuhsite.core.dtos.lecture.response;

import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.OfferingStatisticsDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureStatisticsDto {

    private UUID id;

    private String name;

    private String code;

    private List<OfferingStatisticsDto> offerings;

}
