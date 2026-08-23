package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDao extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR :role MEMBER OF u.authorities)
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<User> search(@Param("role") Role role, @Param("search") String search, Pageable pageable);
}
