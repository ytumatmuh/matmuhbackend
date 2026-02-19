package com.matmuh.matmuhsite.core.dtos.image.response;

import com.matmuh.matmuhsite.entities.Image;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDto {


    private UUID id;

    private String fileName;

    private String fileType;

    private String fileUrl;

    private String fileSize;


    public ImageDto(Image image){
        this.id = image.getId();
        this.fileName = image.getFileName();
        this.fileType = image.getFileType();
        this.fileUrl = image.getFileUrl();
        this.fileSize = String.valueOf(image.getFileSize());
    }

}
