package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserManager implements UserService {

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;


    public UserManager(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Result addUser(User user) {
        if (userDao.findByEmail(user.getEmail()).isPresent()){
            return new ErrorResult(UserMessages.emailAlreadyExists, HttpStatus.CONFLICT);
        }

        if (userDao.findByUsername(user.getUsername()).isPresent()){
            return new ErrorResult(UserMessages.usernameAlreadyExists, HttpStatus.CONFLICT);
        }

        userDao.save(user);
        return new SuccessResult(UserMessages.userAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public DataResult<User> getUserByEmail(String email) {
        var user = userDao.findByEmail(email);

        if(user.isEmpty()){
            return new ErrorDataResult<>(UserMessages.userNotFound, HttpStatus.NOT_FOUND);
        }
        return new SuccessDataResult<User>(user.get(), UserMessages.userListed, HttpStatus.OK);
    }

    @Override
    public DataResult<List<User>> getUsers() {
        var users = userDao.findAll();

        if (users.isEmpty()){
            return new ErrorDataResult<>(UserMessages.usersNotFound, HttpStatus.NOT_FOUND);
        }
        return new SuccessDataResult<List<User>>(users, UserMessages.usersListed, HttpStatus.OK);
    }

    @Override
    public DataResult<User> getUserById(UUID id) {
        var user = userDao.findById(id);

        if(user.isEmpty()){
            return new ErrorDataResult<>(UserMessages.userNotFound, HttpStatus.NOT_FOUND);
        }
        return new SuccessDataResult<User>(user.get(), UserMessages.userListed, HttpStatus.OK);
    }

    @Override
    public DataResult<User> getUserByUsername(String username) {
        var user = userDao.findByUsername(username);

        if(user.isEmpty()){
            return new ErrorDataResult<>(UserMessages.userNotFound, HttpStatus.NOT_FOUND);
        }
        return new SuccessDataResult<User>(user.get(), UserMessages.userListed, HttpStatus.OK);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return getUserByUsername(username).getData();
    }

    @Override
    public Result deleteUserById(UUID id) {
        var user = userDao.findById(id);

        if (user.isEmpty()){
            return new ErrorResult(UserMessages.userNotFound, HttpStatus.NOT_FOUND);
        }

        userDao.delete(user.get());
        return new SuccessResult(UserMessages.userDeleteSuccess, HttpStatus.OK);
    }

    @Override
    public Result updateUserById(User user) {
        var userResult = getUserById(user.getId());

        if (!userResult.isSuccess()){
            return new ErrorResult(userResult.getMessage(), userResult.getHttpStatus());
        }

        if (user.getPassword() != null){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }else {
            user.setPassword(userResult.getData().getPassword());
        }

        userDao.save(user);
        return new SuccessResult(UserMessages.userUpdateSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() == "anonymousUser"){
            return new ErrorDataResult<>(UserMessages.userIsNotAuthenticatedPleaseLogin, HttpStatus.UNAUTHORIZED);
        }
        return getUserByUsername(authentication.getName());

    }

    @Override
    public Result changeAuthenticatedUserPassword(String oldPassword, String newPassword) {
        var userResult = getAuthenticatedUser();

        if (!userResult.isSuccess()){
            return new ErrorResult(userResult.getMessage(), userResult.getHttpStatus());
        }

        if (oldPassword == null || newPassword == null){
            return new ErrorResult(UserMessages.passwordCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        var user = userResult.getData();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            return new ErrorResult(UserMessages.oldPasswordIsIncorrect, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.save(user);

        return new SuccessResult(UserMessages.passwordChangeSuccess, HttpStatus.OK);
    }


}
