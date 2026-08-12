package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CmsLocale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsLocaleDao extends JpaRepository<CmsLocale, String> {

    List<CmsLocale> findAllByOrderByPositionAsc();
}
