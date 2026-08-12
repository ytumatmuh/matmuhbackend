package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentDraftDao extends JpaRepository<ContentDraft, UUID> {


    @Query("""
            SELECT d FROM ContentDraft d
            WHERE d.slug = :slug
              AND d.userId = :userId
              AND ((:locale IS NULL AND d.locale IS NULL) OR d.locale = :locale)
            """)
    Optional<ContentDraft> findOwn(@Param("slug") String slug,
                                   @Param("userId") String userId,
                                   @Param("locale") String locale);

    @Modifying
    @Query("""
            DELETE FROM ContentDraft d
            WHERE d.slug = :slug
              AND d.userId = :userId
              AND ((:locale IS NULL AND d.locale IS NULL) OR d.locale = :locale)
            """)
    void deleteOwn(@Param("slug") String slug,
                   @Param("userId") String userId,
                   @Param("locale") String locale);
}
