package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.user.request.CreateUserRequestDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserDto getUserById(UUID id);

    UserDto createUser(CreateUserRequestDto createUserRequestDto);

    UserDto createUserFromOauth2(User user);

    User getUserEntityByEmail(String email);

    UserDto getUserByEmail(String email);

    List<UserDto> getUsers();

}
