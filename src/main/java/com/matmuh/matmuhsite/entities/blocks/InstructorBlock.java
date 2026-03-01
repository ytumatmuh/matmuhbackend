package com.matmuh.matmuhsite.entities.blocks;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class InstructorBlock implements ContentBlock{

    private List<UUID >instructorIds;

    private Map<String, Object> props;


}
