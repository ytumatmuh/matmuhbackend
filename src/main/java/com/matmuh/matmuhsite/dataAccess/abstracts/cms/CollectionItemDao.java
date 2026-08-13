package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionItemDao extends JpaRepository<CollectionItem, UUID>, CollectionItemDaoCustom {

    Optional<CollectionItem> findByCollectionKeyAndSlugAndArchivedFalse(String collectionKey, String slug);

    Optional<CollectionItem> findByCollectionKeyAndSlug(String collectionKey, String slug);

    boolean existsByCollectionKeyAndSlug(String collectionKey, String slug);

    List<CollectionItem> findByCollectionKeyAndTranslationGroupId(String collectionKey, UUID translationGroupId);

    List<CollectionItem> findByCollectionKeyAndTranslationGroupIdIn(String collectionKey, Collection<UUID> translationGroupIds);
}
