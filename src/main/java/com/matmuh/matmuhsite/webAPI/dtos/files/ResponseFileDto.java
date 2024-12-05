package com.matmuh.matmuhsite.webAPI.dtos.files;

import com.matmuh.matmuhsite.webAPI.dtos.users.ResponseUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseFileDto {

    private UUID id;

    private String name;

    private String type;

    private String url;

    ResponseUserDto createdBy;


}
