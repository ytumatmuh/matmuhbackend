package com.matmuh.matmuhsite.core.dtos.search.response;

import com.matmuh.matmuhsite.entities.SearchResultType;
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
public class SearchGroupDto {

    private SearchResultType type;

    private String label;

    private long total;

    private List<SearchHitDto> items = new ArrayList<>();
}
