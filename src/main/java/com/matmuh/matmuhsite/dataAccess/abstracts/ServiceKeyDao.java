package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.ServiceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceKeyDao extends JpaRepository<ServiceKey, UUID> {

    Optional<ServiceKey> findByKeyPrefix(String keyPrefix);

    List<ServiceKey> findAllByOrderByCreatedAtDesc();
}
