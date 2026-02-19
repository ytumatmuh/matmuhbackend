package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.user.response.SimpleUserDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> userList);

    User toEntity(UserDto userDto);

    SimpleUserDto toSimpleDto(User user);

}
