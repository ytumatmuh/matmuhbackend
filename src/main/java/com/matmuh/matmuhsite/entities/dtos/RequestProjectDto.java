package com.matmuh.matmuhsite.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestProjectDto {
    private int id;
    private String name;
    private String description;
    private String context;
    private Date date;

}
