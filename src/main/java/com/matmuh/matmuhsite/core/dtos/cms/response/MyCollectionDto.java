package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.entities.cms.SlugSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyCollectionDto {
    private String collectionKey;
    private CollectionSchema schema;
    private boolean canCreate;
    private SlugSource slugSource;

    private List<String> locales;
}
