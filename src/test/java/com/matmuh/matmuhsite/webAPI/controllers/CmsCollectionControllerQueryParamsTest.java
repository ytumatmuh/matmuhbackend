package com.matmuh.matmuhsite.webAPI.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

// UnknownQueryParameterInterceptor yalnız @RequestParam ile bildirilen parametreleri kabul eder;
// SDK'nın (defaults/transport.js) gönderdiği her sorgu parametresi burada bildirilmiş olmalı.
class CmsCollectionControllerQueryParamsTest {

    @Test
    void newDraftAcceptsWhatTheSdkSends() {
        assertDeclares("saveNewDraft", "locale", "translationGroup");
        assertDeclares("deleteNewDraft", "locale");
    }

    @Test
    void createAndUpsertAcceptWhatTheSdkSends() {
        assertDeclares("create", "locale", "translationGroup");
        assertDeclares("upsert", "locale");
        assertDeclares("archive", "version");
        assertDeclares("renameSlug", "replaceAlias");
        assertDeclares("lookup", "q", "slugs", "locale", "limit");
    }

    private static void assertDeclares(String method, String... params) {
        var declared = Arrays.stream(CmsCollectionController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(method))
                .findFirst()
                .map(CmsCollectionControllerQueryParamsTest::requestParams)
                .orElseThrow();
        assertTrue(declared.containsAll(Set.of(params)), method + " declares " + declared);
    }

    private static Set<String> requestParams(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(RequestParam.class))
                .map(parameter -> {
                    var annotation = parameter.getAnnotation(RequestParam.class);
                    var name = annotation.value().isBlank() ? annotation.name() : annotation.value();
                    return name.isBlank() ? parameter.getName() : name;
                })
                .collect(Collectors.toSet());
    }
}
