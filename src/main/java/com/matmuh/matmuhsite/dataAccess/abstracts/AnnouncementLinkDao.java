package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.AnnouncementLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementLinkDao extends JpaRepository<AnnouncementLink, Integer> {
    List<AnnouncementLink> findByAnnouncementId(int announcementId);
}
