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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthManager implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;


    @Override
    public DataResult<String> login(RequestLoginDto requestLoginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestLoginDto.getUsername(), requestLoginDto.getPassword()));
        if (authentication.isAuthenticated()) {
            var token = jwtService.generateToken(requestLoginDto.getUsername());
            return new SuccessDataResult<String>(token, AuthMessages.loginSuccess);
        }

        return new ErrorDataResult<>(AuthMessages.invalidUsernameOrPassword);

    }

    @Override
    public Result register(RequestRegisterDto requestRegisterDto) {
        if(requestRegisterDto.getFirstName() == null){
            return new ErrorDataResult<>(AuthMessages.firstNameCannotBeNull);
        }

        if(requestRegisterDto.getLastName() == null){
            return new ErrorDataResult<>(AuthMessages.lastNameCannotBeNull);
        }

        if(requestRegisterDto.getUsername() == null){
            return new ErrorDataResult<>(AuthMessages.usernameCannotBeNull);
        }

        if(requestRegisterDto.getEmail() == null){
            return new ErrorDataResult<>(AuthMessages.emailCannotBeNull);
        }

        if(requestRegisterDto.getPassword() == null){
            return new ErrorDataResult<>(AuthMessages.passwordCannotBeNull);
        }

        if(requestRegisterDto.getPassword().length() < 6){
            return new ErrorDataResult<>(AuthMessages.passwordLengthMustBeGreaterThanSix);
        }

        var user = User.builder()
                .firstName(requestRegisterDto.getFirstName())
                .lastName(requestRegisterDto.getLastName())
                .username(requestRegisterDto.getUsername())
                .email(requestRegisterDto.getEmail())
                .password(requestRegisterDto.getPassword())
                .build();


        return userService.addUser(user);


    }


}
