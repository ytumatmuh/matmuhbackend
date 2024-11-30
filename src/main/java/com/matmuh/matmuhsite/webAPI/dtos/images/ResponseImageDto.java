package com.matmuh.matmuhsite.webAPI.dtos.images;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseImageDto {

    private int id;

    private int name;

    private String type;

    private String url;
}
