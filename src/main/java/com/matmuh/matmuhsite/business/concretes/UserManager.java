package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManager implements UserService {




    @Override
    public Result addUser(User user) {

    }

    @Override
    public DataResult<List<User>> getUsers() {
        return null;
    }

    @Override
    public DataResult<User> getUserById(int id) {
        return null;
    }

    @Override
    public DataResult<User> getByUsername(String username) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return null;
    }
}
