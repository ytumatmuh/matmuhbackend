package com.matmuh.matmuhsite.core.dtos.cms.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContentResponseDto {
    private String slug;
    private List<BlockDto> blocks;
}