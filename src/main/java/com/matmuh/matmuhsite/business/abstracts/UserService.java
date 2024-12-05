package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    Result addUser(User user);

    DataResult<User> getUserByEmail(String email);

    DataResult<List<User>> getUsers();

    DataResult<User> getUserById(UUID id);

    DataResult<User> getUserByUsername(String username);

    UserDetails loadUserByUsername(String username);

    Result deleteUserById(UUID id);

    Result updateUserById(User user);

    DataResult<User> getAuthenticatedUser();

    Result changeAuthenticatedUserPassword(String oldPassword, String newPassword);


}
