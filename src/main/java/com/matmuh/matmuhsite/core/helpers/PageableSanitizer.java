package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableSanitizer {

    private PageableSanitizer() {}

    public static Pageable sanitize(Pageable pageable, Set<String> allowedSortFields, String defaultSortField) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new BusinessRuleException("error.sort.field.invalid");
            }
        }

        var sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.ASC, defaultSortField);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
