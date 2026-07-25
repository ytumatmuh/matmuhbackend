package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentDraftDao extends JpaRepository<ContentDraft, UUID> {

    Optional<ContentDraft> findBySlugAndUserId(String slug, String userId);

    void deleteBySlugAndUserId(String slug, String userId);
}