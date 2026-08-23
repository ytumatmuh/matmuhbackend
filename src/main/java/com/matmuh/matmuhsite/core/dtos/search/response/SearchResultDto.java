package com.matmuh.matmuhsite.core.dtos.search.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {

    private String query;

    private long total;

    private List<SearchGroupDto> groups = new ArrayList<>();
}
