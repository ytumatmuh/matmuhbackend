package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManager implements UserService {

    //YAZILACAK yazıyom zaaa

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

    @Override
    public Result deleteUser(int id) {
        return null;
    }
}
