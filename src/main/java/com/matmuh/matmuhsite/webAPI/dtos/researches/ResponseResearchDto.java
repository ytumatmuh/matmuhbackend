package com.matmuh.matmuhsite.webAPI.dtos.researches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseResearchDto {

    private UUID id;

    private String title;

    private String description;

    private String coverImageUrl;


}
