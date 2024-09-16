package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    Result addUser(User user);

    DataResult<List<User>> getUsers();

    DataResult<User> getUserById(int id);

    DataResult<User> getByUsername(String username);

    UserDetails loadUserByUsername(String username);

    Result deleteUser(int id);

    DataResult<User> getAuthenticatedUser();


}
