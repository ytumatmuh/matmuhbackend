package com.matmuh.matmuhsite.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "images")
public class Image{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @JsonIgnore
    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
