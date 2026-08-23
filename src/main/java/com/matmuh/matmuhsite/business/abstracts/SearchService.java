package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.search.response.SearchResultDto;
import com.matmuh.matmuhsite.entities.SearchResultType;

import java.util.Set;

public interface SearchService {

    SearchResultDto search(String query, Set<SearchResultType> types, String locale, int limit);
}
