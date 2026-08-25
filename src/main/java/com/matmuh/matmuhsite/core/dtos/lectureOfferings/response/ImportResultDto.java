package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportResultDto {

    private int total;
    private int created;
    private int updated;
    private int failed;

    private List<RowResult> results;

    public enum Status {
        CREATED, UPDATED, FAILED
    }

    public record RowKey(String lectureCode, String academicYear, Semester semester, int groupNumber) {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RowResult {

        private int index;
        private RowKey key;
        private Status status;

        private UUID offeringId;
        private String error;


        private List<String> warnings;
    }
}
