package com.matmuh.matmuhsite.entities.blocks;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TextBlock implements ContentBlock {
    private String text;
    private Map<String, Object> props;
}
