package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionDraftDao extends JpaRepository<CollectionDraft, UUID> {

    Optional<CollectionDraft> findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(
            String collectionKey, String slug, String userId);

    Optional<CollectionDraft> findByCollectionKeyAndUserIdAndForNewItemTrue(
            String collectionKey, String userId);
}
