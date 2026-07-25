package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentBlockDao extends JpaRepository<ContentBlock, UUID> {

    List<ContentBlock> findBySlugAndArchivedFalseOrderBySortOrderAsc(String slug);

    List<ContentBlock> findBySlugAndBlockPathIn(String slug, List<String> blockPaths);
}