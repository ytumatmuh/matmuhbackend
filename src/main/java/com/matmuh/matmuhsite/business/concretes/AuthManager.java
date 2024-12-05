package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthManager implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;


    public AuthManager(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public DataResult<String> login(User user) {

        if (user.getEmail() == null && user.getUsername() == null){
            return new ErrorDataResult<>(AuthMessages.emailOrUsernameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if (user.getPassword() == null){
            return new ErrorDataResult<>(AuthMessages.passwordCannotBeNull, HttpStatus.BAD_REQUEST);
        }



        switch (user.getEmail() == null ? "username" : "email"){
            case "username":
                var userByUsername = userService.getUserByUsername(user.getUsername());
                if (!userByUsername.isSuccess()){
                    return new ErrorDataResult<>(userByUsername.getMessage(), userByUsername.getHttpStatus());
                }
                user.setUsername(userByUsername.getData().getUsername());
                break;
            case "email":
                var userByEmail = userService.getUserByEmail(user.getEmail());
                if (!userByEmail.isSuccess()){
                    return new ErrorDataResult<>(userByEmail.getMessage(), userByEmail.getHttpStatus());
                }
                user.setUsername(userByEmail.getData().getUsername());
                break;
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
        if (authentication.isAuthenticated()) {
            var userLoggedIn = userService.getUserByUsername(user.getUsername()).getData();

            var token = jwtService.generateToken(userLoggedIn);

            return new SuccessDataResult<>(token, AuthMessages.loginSuccess, HttpStatus.CREATED);
        }

        return new ErrorDataResult<>(AuthMessages.invalidUsernameOrPassword, HttpStatus.BAD_REQUEST);

    }

    @Override
    public Result register(User user) {

        if(user.getFirstName() == null){
            return new ErrorDataResult<>(AuthMessages.firstNameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getLastName() == null){
            return new ErrorDataResult<>(AuthMessages.lastNameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getUsername() == null){
            return new ErrorDataResult<>(AuthMessages.usernameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getEmail() == null){
            return new ErrorDataResult<>(AuthMessages.emailCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getPassword() == null){
            return new ErrorDataResult<>(AuthMessages.passwordCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getPassword().length() < 6){
            return new ErrorDataResult<>(AuthMessages.passwordLengthMustBeGreaterThanSix, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthorities(Set.of(Role.ROLE_USER));

        return userService.addUser(user);


    }


}
