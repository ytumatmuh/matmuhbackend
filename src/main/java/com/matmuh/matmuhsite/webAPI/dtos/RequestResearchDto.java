package com.matmuh.matmuhsite.webAPI.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestResearchDto {
    private UUID id;
    private String title;
    private String description;

}
