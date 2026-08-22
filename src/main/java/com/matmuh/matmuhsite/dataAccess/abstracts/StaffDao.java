package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffDao extends JpaRepository<Staff, UUID> {

    @Query("""
            SELECT s FROM Staff s
            WHERE (CAST(:search AS String) IS NULL
                   OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(s.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(CONCAT(s.lastName, ' ', s.firstName)) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
              AND (:group IS NULL OR :group MEMBER OF s.groups)
              AND (CAST(:academicTitle AS String) IS NULL
                   OR LOWER(s.academicTitle) = LOWER(CAST(:academicTitle AS String)))
            """)
    Page<Staff> search(@Param("search") String search,
                       @Param("group") StaffGroup group,
                       @Param("academicTitle") String academicTitle,
                       Pageable pageable);



    @Query("""
            SELECT s FROM Staff s
            WHERE (CAST(:firstName AS String) IS NULL
                   OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS String), '%')))
              AND (CAST(:lastName AS String) IS NULL
                   OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS String), '%')))
              AND (CAST(:email AS String) IS NULL
                   OR LOWER(s.email) LIKE LOWER(CONCAT('%', CAST(:email AS String), '%')))
              AND (CAST(:avesisLink AS String) IS NULL
                   OR LOWER(s.avesisLink) LIKE LOWER(CONCAT('%', CAST(:avesisLink AS String), '%')))
              AND (:group IS NULL OR :group MEMBER OF s.groups)
            """)
    Page<Staff> filter(@Param("firstName") String firstName,
                            @Param("lastName") String lastName,
                            @Param("email") String email,
                            @Param("avesisLink") String avesisLink,
                            @Param("group") StaffGroup group,
                            Pageable pageable);

    Optional<Staff> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
