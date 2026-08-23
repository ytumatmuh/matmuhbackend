package com.matmuh.matmuhsite.core.dtos.lectureNote.response;

import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteWithLectureDto extends LectureNoteDto{


    private LectureSummaryDto lecture;

}
