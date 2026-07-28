package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE instructors SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "instructors")
public class Instructor extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "academic_title")
    private String academicTitle;

    @Column(name = "slug")
    private String slug;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "email")
    private String email;

    @Column(name = "avesis_link")
    private String avesisLink;

    @Column(name = "office")
    private String office;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureOffering> offerings = new ArrayList<>();

}
