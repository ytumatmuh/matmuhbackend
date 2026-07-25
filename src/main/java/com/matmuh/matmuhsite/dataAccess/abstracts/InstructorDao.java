package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Instructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstructorDao extends JpaRepository<Instructor, UUID> {

    @Query("""
            SELECT i FROM Instructor i
            WHERE CAST(:search AS String) IS NULL
               OR LOWER(i.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
               OR LOWER(i.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
               OR LOWER(i.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
               OR LOWER(CONCAT(i.firstName, ' ', i.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
               OR LOWER(CONCAT(i.lastName, ' ', i.firstName)) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
            """)
    Page<Instructor> search(@Param("search") String search, Pageable pageable);
}
