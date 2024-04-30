package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Research;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchDao extends JpaRepository<Research, Integer> {
    Research findById(int id);

    List<Research> findAll();

    Page<Research> findAll(Pageable pageable);
}
