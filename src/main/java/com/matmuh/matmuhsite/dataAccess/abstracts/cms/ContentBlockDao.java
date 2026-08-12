package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentBlockDao extends JpaRepository<ContentBlock, UUID> {

    // locale null iken dilsiz satırları getirir; bu, dil bildirilmemiş kurulumun davranışı.
    @Query("""
            SELECT b FROM ContentBlock b
            WHERE b.slug = :slug
              AND b.archived = false
              AND ((:locale IS NULL AND b.locale IS NULL) OR b.locale = :locale)
            ORDER BY b.sortOrder ASC
            """)
    List<ContentBlock> findPublished(@Param("slug") String slug, @Param("locale") String locale);

    @Query("""
            SELECT b FROM ContentBlock b
            WHERE b.slug = :slug
              AND b.blockPath IN :blockPaths
              AND ((:locale IS NULL AND b.locale IS NULL) OR b.locale = :locale)
            """)
    List<ContentBlock> findForUpdate(@Param("slug") String slug,
                                     @Param("blockPaths") List<String> blockPaths,
                                     @Param("locale") String locale);
}
