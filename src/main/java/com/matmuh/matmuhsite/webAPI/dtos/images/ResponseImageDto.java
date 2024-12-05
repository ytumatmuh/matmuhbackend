package com.matmuh.matmuhsite.webAPI.dtos.images;

import com.matmuh.matmuhsite.webAPI.dtos.users.ResponseUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseImageDto {

    private UUID id;

    private String name;

    private String type;

    private String url;

    private ResponseUserDto createdBy;
}
