package com.matmuh.matmuhsite.webAPI.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestEventDto {

    private int id;
    private String name;
    private Date date;
    private String context;
    private int userId;

}
