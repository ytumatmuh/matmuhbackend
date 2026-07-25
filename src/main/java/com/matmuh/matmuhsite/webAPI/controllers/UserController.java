package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.mappers.UserMapper;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Kullanıcı işlemleri")
@RestController
@RequestMapping("api/users")
public class UserController {

    private final SecurityService securityService;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;

    public UserController(SecurityService securityService, UserMapper userMapper, MessageResolver messageResolver) {
        this.securityService = securityService;
        this.userMapper = userMapper;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Mevcut kullanıcı", description = "Giriş yapmış kullanıcının bilgilerini döner.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<UserDto>> getCurrentUser() {
        var user = securityService.getAuthenticatedUserFromDatabase();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessDataResult<>(userMapper.toDto(user), messageResolver.resolve(UserMessages.USER_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }
}
