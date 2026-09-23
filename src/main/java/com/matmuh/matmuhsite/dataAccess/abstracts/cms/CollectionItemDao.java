package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionItemDao extends JpaRepository<CollectionItem, UUID>, CollectionItemDaoCustom {

    interface LiveItemCount {
        String getCollectionKey();

        String getLocale();

        long getCount();
    }

    @Query("""
            SELECT i.collectionKey AS collectionKey, i.locale AS locale, COUNT(i) AS count
            FROM CollectionItem i
            WHERE i.archived = false AND i.collectionKey IN :collectionKeys
            GROUP BY i.collectionKey, i.locale
            """)
    List<LiveItemCount> countLiveByCollection(@Param("collectionKeys") Collection<String> collectionKeys);

    Optional<CollectionItem> findByCollectionKeyAndSlugAndArchivedFalse(String collectionKey, String slug);

    Optional<CollectionItem> findByCollectionKeyAndSlug(String collectionKey, String slug);

    boolean existsByCollectionKeyAndSlug(String collectionKey, String slug);

    List<CollectionItem> findByCollectionKeyAndArchivedTrue(String collectionKey);

    List<CollectionItem> findByCollectionKeyAndTranslationGroupId(String collectionKey, UUID translationGroupId);

    List<CollectionItem> findByCollectionKeyAndTranslationGroupIdIn(String collectionKey, Collection<UUID> translationGroupIds);
}
