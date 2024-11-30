package com.matmuh.matmuhsite.webAPI.dtos.images;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestImageDto {
    private String photoUrl;
}
