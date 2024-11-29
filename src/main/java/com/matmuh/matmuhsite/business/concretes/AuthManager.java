package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.User;
import com.matmuh.matmuhsite.entities.dtos.RequestLoginDto;
import com.matmuh.matmuhsite.entities.dtos.RequestRegisterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public DataResult<?> login(User user) {

        if (user.getEmail().isEmpty() && user.getUsername().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.emailOrUsernameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if (user.getPassword().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.passwordCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        switch (user.getEmail().isEmpty() ? "username" : "email"){
            case "username":
                var userByUsername = userService.getUserByUsername(user.getUsername());
                if (!userByUsername.isSuccess()){
                    return userByUsername;
                }
                user = userByUsername.getData();
                break;
            case "email":
                var userByEmail = userService.getUserByEmail(user.getEmail());
                if (!userByEmail.isSuccess()){
                    return userByEmail;
                }
                user = userByEmail.getData();
                break;
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword()));
        if (authentication.isAuthenticated()) {
            var token = jwtService.generateToken(user.getEmail(), user.getAuthorities());

            return new SuccessDataResult<>(token, AuthMessages.loginSuccess, HttpStatus.CREATED);
        }

        return new ErrorDataResult<>(AuthMessages.invalidUsernameOrPassword, HttpStatus.BAD_REQUEST);

    }

    @Override
    public Result register(User user) {

        if(user.getFirstName().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.firstNameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getLastName().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.lastNameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getUsername().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.usernameCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getEmail().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.emailCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getPassword().isEmpty()){
            return new ErrorDataResult<>(AuthMessages.passwordCannotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(user.getPassword().length() < 6){
            return new ErrorDataResult<>(AuthMessages.passwordLengthMustBeGreaterThanSix, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userService.addUser(user);


    }


}
