package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.dtos.user.request.CreateUserRequestDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.UserMapper;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserManager implements UserService {

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;


    private final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private final UserMapper userMapper;


    public UserManager(UserDao userDao, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto getUserById(UUID id) {
        logger.info("Getting user by id: {}", id);
        User user = userDao.findById(id).orElseThrow(() -> {
            logger.warn("User not found with id: {}", id);
            return new ResourceNotFoundException(UserMessages.USER_NOT_FOUND_WITH_ID);
        });

        return userMapper.toDto(user);
    }

    @Override
    public UserDto createUser(CreateUserRequestDto createUserRequestDto) {
        logger.info("Creating user with email: {}", createUserRequestDto.getEmail());

        User user = new User();
        user.setFirstName(createUserRequestDto.getFirstName());
        user.setLastName(createUserRequestDto.getLastName());
        user.setEmail(createUserRequestDto.getEmail());
        user.setAuthorities(Set.of(Role.ROLE_USER));
        if (createUserRequestDto.getPassword() != null){
            logger.info("");
            user.setPassword(passwordEncoder.encode(createUserRequestDto.getPassword()));
        }

        logger.info("User created with email: {}, userId: {}", createUserRequestDto.getEmail(), user.getId());

        var savedUser = userDao.save(user);

        UserDto userDto = new UserDto();
        userDto.setId(savedUser.getId());
        userDto.setEmail(savedUser.getEmail());
        userDto.setFirstName(savedUser.getFirstName());
        userDto.setLastName(savedUser.getLastName());
        return userDto;

    }

    @Override
    public UserDto createUserFromOauth2(User user) {
        logger.info("Creating user from OAuth2 with email: {}", user.getEmail());
        var savedUser = userDao.save(user);
        UserDto userDto = new UserDto();
        userDto.setId(savedUser.getId());
        userDto.setEmail(savedUser.getEmail());
        userDto.setFirstName(savedUser.getFirstName());
        userDto.setLastName(savedUser.getLastName());
        logger.info("User created from OAuth2 with email: {}, userId: {}", user.getEmail(), savedUser.getId());
        return userDto;
    }

    @Override
    public User getUserEntityByEmail(String email) {
        logger.info("Getting user entity by email: {}", email);
        return userDao.findByEmail(email).orElseThrow(() -> {
            logger.warn("User entity not found with email: {}", email);
            return new ResourceNotFoundException(UserMessages.USER_NOT_FOUND_WITH_EMAIL);
        });
    }

    @Override
    public UserDto getUserByEmail(String email) {
        logger.info("Getting user by email: {}", email);

        User user = userDao.findByEmail(email).orElseThrow(() -> {
            logger.info("User not found with email: {}", email);
            throw new  ResourceNotFoundException(UserMessages.USER_NOT_FOUND_WITH_EMAIL);
        });

        return userMapper.toDto(user);

    }

    @Override
    public List<UserDto> getUsers() {
        logger.info("Getting all users");
        List<User> users = userDao.findAll();
        return userMapper.toDtoList(users);
    }


    @Override
    public UserDetails loadUserByUsername(String username) {
        return getUserEntityByEmail(username);
    }


}
