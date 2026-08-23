package com.matmuh.matmuhsite.core.dtos.search.response;

import com.matmuh.matmuhsite.entities.SearchResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchHitDto {

    private SearchResultType type;

    private String id;

    private String slug;

    private String title;

    private String subtitle;
}
