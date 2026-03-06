package com.matmuh.matmuhsite.entities.blocks;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ImageBlock implements ContentBlock{

    private UUID imageId;

    private Map<String, Object> props;

}

//matris blok (tablo), video blok,