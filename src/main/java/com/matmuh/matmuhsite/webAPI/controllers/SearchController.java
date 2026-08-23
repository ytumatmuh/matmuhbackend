package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.SearchService;
import com.matmuh.matmuhsite.business.constants.SearchMessages;
import com.matmuh.matmuhsite.core.dtos.search.response.SearchResultDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.SearchResultType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "Search", description = "Site geneli arama")
@RestController
@RequestMapping("api/search")
public class SearchController {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 25;

    private final SearchService searchService;
    private final MessageResolver messageResolver;

    public SearchController(SearchService searchService, MessageResolver messageResolver) {
        this.searchService = searchService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Site geneli arama",
            description = "Ders, seçmeli grubu, personel, duyuru ve haberlerde arar; sonuçları türe göre gruplayıp döner. "
                    + "types ile aranacak türler daraltılabilir (LECTURE, ELECTIVE_GROUP, STAFF, ANNOUNCEMENT, NEWS). "
                    + "limit her grup için tavan sonuç sayısıdır; total ise o türdeki gerçek eşleşme sayısını verir. "
                    + "Duyuru ve haberlerde locale ile dil seçilir, boş bırakılırsa varsayılan dil kullanılır.")
    @GetMapping
    public ResponseEntity<DataResult<SearchResultDto>> search(
            @RequestParam String q,
            @RequestParam(required = false) Set<SearchResultType> types,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false, defaultValue = "" + DEFAULT_LIMIT) int limit) {
        var bounded = Math.max(1, Math.min(limit, MAX_LIMIT));
        var result = searchService.search(q, types, locale, bounded);
        return ResponseEntity.ok(new SuccessDataResult<>(result,
                messageResolver.resolve(SearchMessages.SEARCH_COMPLETED), HttpStatus.OK));
    }
}
