package com.matmuh.matmuhsite.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageRef {

    @Column(name = "src")
    private String src;

    @Column(name = "alt")
    private String alt;
}
