package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementDao extends JpaRepository<Announcement, UUID> {

    Page<Announcement> findAll(Pageable pageable);

    List<Announcement> findAll();

   //Optional<Announcement> findAll(Specification<Announcement> announcementSpecification, Page<Announcement> page);

}
