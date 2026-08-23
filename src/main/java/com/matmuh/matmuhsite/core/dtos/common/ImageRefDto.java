package com.matmuh.matmuhsite.core.dtos.common;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageRefDto {

    @Size(max = 255, message = "{image.src.too.long}")
    private String src;

    @Size(max = 255, message = "{image.alt.too.long}")
    private String alt;
}
