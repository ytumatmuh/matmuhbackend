package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.User;
import com.matmuh.matmuhsite.webAPI.dtos.users.RequestChangePaswordDto;
import com.matmuh.matmuhsite.webAPI.dtos.users.RequestUserDto;
import com.matmuh.matmuhsite.webAPI.dtos.users.ResponseUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/changeAuthenticatedUserPassword")
    public ResponseEntity<Result> changePassword(@RequestBody RequestChangePaswordDto requestChangePaswordDto){
        var result = userService.changeAuthenticatedUserPassword(requestChangePaswordDto.getOldPassword(), requestChangePaswordDto.getNewPassword());
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @DeleteMapping("/deleteUserById/{id}")
    public ResponseEntity<Result> deleteUserById(@PathVariable UUID id){
        var result = userService.deleteUserById(id);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @GetMapping("/getAuthenticatedUser")
    public ResponseEntity<DataResult<ResponseUserDto>> getAuthenticatedUser(){
        var result = userService.getAuthenticatedUser();

        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(new ErrorDataResult<>(result.getMessage(), result.getHttpStatus()));
        }

        ResponseUserDto responseUserDto = ResponseUserDto.builder()
                .id(result.getData().getId())
                .firstName(result.getData().getFirstName())
                .lastName(result.getData().getLastName())
                .username(result.getData().getUsername())
                .email(result.getData().getEmail())
                .authorities(result.getData().getAuthorities())
                .build();


        return ResponseEntity.status(result.getHttpStatus()).body(new SuccessDataResult<>(responseUserDto, result.getMessage(), result.getHttpStatus()));
    }

    @PutMapping("/updateUserById/{id}")
    public ResponseEntity<Result> updateUserById(@PathVariable UUID id, @RequestBody RequestUserDto responseUserDto){

        User user = User.builder()
                .id(id)
                .firstName(responseUserDto.getFirstName())
                .lastName(responseUserDto.getLastName())
                .username(responseUserDto.getUsername())
                .email(responseUserDto.getEmail())
                .password(responseUserDto.getPassword())
                .authorities(responseUserDto.getAuthorities())
                .build();


        var result = userService.updateUserById(user);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<DataResult<List<ResponseUserDto>>> getUsers(){
        var result = userService.getUsers();

        if (!result.isSuccess()){
            return ResponseEntity.status(result.getHttpStatus()).body(new ErrorDataResult<>(result.getMessage(), result.getHttpStatus()));
        }

        List<ResponseUserDto> responseUserDtos = result.getData().stream().map(user -> ResponseUserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(user.getAuthorities())
                .build()).toList();

        return ResponseEntity.status(result.getHttpStatus()).body(new SuccessDataResult<>(responseUserDtos, result.getMessage(), result.getHttpStatus()));
    }

}
