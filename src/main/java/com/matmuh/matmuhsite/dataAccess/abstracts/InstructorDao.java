package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Instructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
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



    @Query("""
            SELECT i FROM Instructor i
            WHERE (CAST(:firstName AS String) IS NULL
                   OR LOWER(i.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS String), '%')))
              AND (CAST(:lastName AS String) IS NULL
                   OR LOWER(i.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS String), '%')))
              AND (CAST(:email AS String) IS NULL
                   OR LOWER(i.email) LIKE LOWER(CONCAT('%', CAST(:email AS String), '%')))
              AND (CAST(:avesisLink AS String) IS NULL
                   OR LOWER(i.avesisLink) LIKE LOWER(CONCAT('%', CAST(:avesisLink AS String), '%')))
            """)
    Page<Instructor> filter(@Param("firstName") String firstName,
                            @Param("lastName") String lastName,
                            @Param("email") String email,
                            @Param("avesisLink") String avesisLink,
                            Pageable pageable);

    Optional<Instructor> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
