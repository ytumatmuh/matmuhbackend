package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionSlugAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionSlugAliasDao extends JpaRepository<CollectionSlugAlias, UUID> {

    Optional<CollectionSlugAlias> findByCollectionKeyAndSlug(String collectionKey, String slug);

    boolean existsByCollectionKeyAndSlug(String collectionKey, String slug);

    List<CollectionSlugAlias> findByCollectionKeyAndItemId(String collectionKey, UUID itemId);

    void deleteByCollectionKeyAndItemId(String collectionKey, UUID itemId);
}
