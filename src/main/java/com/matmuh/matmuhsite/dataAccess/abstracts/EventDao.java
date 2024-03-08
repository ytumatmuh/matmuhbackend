package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDao extends JpaRepository<Event, Integer> {
    Event findById(int id);
}
