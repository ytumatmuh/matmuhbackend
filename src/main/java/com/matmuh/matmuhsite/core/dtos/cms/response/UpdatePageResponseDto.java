package com.matmuh.matmuhsite.core.dtos.cms.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePageResponseDto {
    private int updated;
    private int unchanged;
}
