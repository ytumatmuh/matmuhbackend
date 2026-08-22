package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.entities.cms.SlugSource;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class CollectionRegistry {

    public record CollectionDefinition(
            String key,
            CollectionSchema schema,
            SlugSource slugSource,
            String slugSourceField,
            boolean allowAnonymousRead,
            boolean localized
    ) {}

    private static final List<CollectionDefinition> DEFINITIONS = List.of(
            new CollectionDefinition(
                    LectureCollectionSchema.KEY,
                    LectureCollectionSchema.SCHEMA,
                    SlugSource.AUTO_GENERATED,
                    LectureCollectionSchema.SLUG_SOURCE_FIELD,
                    true,
                    false),
            new CollectionDefinition(
                    StaffCollectionSchema.KEY,
                    StaffCollectionSchema.SCHEMA,
                    SlugSource.AUTO_GENERATED,
                    StaffCollectionSchema.SLUG_SOURCE_FIELD,
                    true,
                    false
            ),
            new CollectionDefinition(
                    AnnouncementCollectionSchema.KEY,
                    AnnouncementCollectionSchema.SCHEMA,
                    SlugSource.AUTO_GENERATED,
                    AnnouncementCollectionSchema.SLUG_SOURCE_FIELD,
                    true,
                    true),
            new CollectionDefinition(
                    NewsCollectionSchema.KEY,
                    NewsCollectionSchema.SCHEMA,
                    SlugSource.AUTO_GENERATED,
                    NewsCollectionSchema.SLUG_SOURCE_FIELD,
                    true,
                    true)
    );

    private final Map<String, CollectionDefinition> byKey =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public CollectionRegistry() {
        for (var definition : DEFINITIONS) {
            var previous = byKey.put(definition.key(), definition);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate collection definition: " + definition.key());
            }
        }
    }

    public CollectionDefinition resolve(String collectionKey) {
        var definition = collectionKey == null ? null : byKey.get(collectionKey.trim());
        if (definition == null) {
            throw new ResourceNotFoundException(CmsMessages.COLLECTION_NOT_FOUND + collectionKey);
        }
        return definition;
    }

    public boolean exists(String collectionKey) {
        return collectionKey != null && byKey.containsKey(collectionKey.trim());
    }

    public Collection<CollectionDefinition> all() {
        return byKey.values();
    }
}
