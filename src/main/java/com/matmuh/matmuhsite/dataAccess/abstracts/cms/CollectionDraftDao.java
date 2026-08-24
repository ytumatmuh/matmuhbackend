package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionDraftDao extends JpaRepository<CollectionDraft, UUID> {

    // locale null iken dilsiz satırları hedefler; dil bildirilmemiş kurulumun davranışı bu.
    @Query("""
            SELECT d FROM CollectionDraft d
            WHERE d.collectionKey = :collectionKey
              AND d.slug = :slug
              AND d.userId = :userId
              AND d.forNewItem = false
              AND ((:locale IS NULL AND d.locale IS NULL) OR d.locale = :locale)
            """)
    Optional<CollectionDraft> findOwnItemDraft(@Param("collectionKey") String collectionKey,
                                               @Param("slug") String slug,
                                               @Param("userId") String userId,
                                               @Param("locale") String locale);

    @Query("""
            SELECT d FROM CollectionDraft d
            WHERE d.collectionKey = :collectionKey
              AND d.userId = :userId
              AND d.forNewItem = true
              AND ((:locale IS NULL AND d.locale IS NULL) OR d.locale = :locale)
            """)
    Optional<CollectionDraft> findOwnNewDraft(@Param("collectionKey") String collectionKey,
                                              @Param("userId") String userId,
                                              @Param("locale") String locale);


    @Query("""
            SELECT d FROM CollectionDraft d
            WHERE d.collectionKey = :collectionKey
              AND d.slug IN :slugs
              AND d.userId = :userId
              AND d.forNewItem = false
              AND ((:locale IS NULL AND d.locale IS NULL) OR d.locale = :locale)
            """)
    List<CollectionDraft> findOwnItemDrafts(@Param("collectionKey") String collectionKey,
                                            @Param("slugs") Collection<String> slugs,
                                            @Param("userId") String userId,
                                            @Param("locale") String locale);

    List<CollectionDraft> findByCollectionKeyAndSlug(String collectionKey, String slug);
}
