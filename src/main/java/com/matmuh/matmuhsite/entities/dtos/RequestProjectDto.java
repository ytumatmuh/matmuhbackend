package com.matmuh.matmuhsite.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestProjectDto {
    private UUID id;
    private String name;
    private String description;
    private Date date;

}
