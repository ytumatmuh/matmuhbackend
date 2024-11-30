package com.matmuh.matmuhsite.webAPI.dtos.projects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestProjectDto {
    private UUID id;
    private String name;
    private String description;

}
