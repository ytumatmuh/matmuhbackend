package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManager implements UserService {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDao userDao;

    @Override
    public Result addUser(User user) {
        if (user.getFirstName() == null){
            return new ErrorResult(UserMessages.firstNameCannotBeNull);
        }
        if (user.getLastName() == null){
            return new ErrorResult(UserMessages.lastNameCannotBeNull);
        }
        if (user.getUsername() == null){
            return new ErrorResult(UserMessages.usernameCannotBeNull);
        }
        if (user.getEmail() == null){
            return new ErrorResult(UserMessages.emailCannotBeNull);
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);
        return new SuccessResult(UserMessages.userAddSuccess);
    }

    @Override
    public DataResult<List<User>> getUsers() {
        var users = userDao.findAll();

        if (users.isEmpty()){
            return new ErrorDataResult<>(UserMessages.usersNotFound);
        }
        return new SuccessDataResult<List<User>>(users, UserMessages.usersListed);
    }

    @Override
    public DataResult<User> getUserById(int id) {
        var user = userDao.findById(id);

        if(user == null){
            return new ErrorDataResult<>(UserMessages.userNotFound);
        }
        return new SuccessDataResult<User>(user, UserMessages.userListed);
    }

    @Override
    public DataResult<User> getByUsername(String username) {
        var user = userDao.findByUsername(username);

        if(user == null){
            return new ErrorDataResult<>(UserMessages.userNotFound);
        }
        return new SuccessDataResult<User>(user, UserMessages.userListed);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = getByUsername(username).getData();
        return user;
    }

    @Override
    public Result deleteUser(int id) {
        var user = userDao.findById(id);

        if (user == null){
            return new ErrorResult(UserMessages.userNotFound);
        }

        userDao.delete(user);
        return new SuccessResult(UserMessages.userDeleteSuccess);
    }
}
