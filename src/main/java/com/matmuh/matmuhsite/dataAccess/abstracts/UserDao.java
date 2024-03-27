package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(int id);
}
