package com.matmuh.matmuhsite.entities.blocks;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class FileBlock implements ContentBlock{

    private UUID fileId;

    private Map<String, Object> props;

}
