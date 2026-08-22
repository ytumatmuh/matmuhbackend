package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE staff SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "staff")
public class Staff extends BaseEntity{

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

    @ElementCollection(targetClass = StaffGroup.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "staff_groups", joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "staff_group", nullable = false)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<StaffGroup> groups = new LinkedHashSet<>();

    @Column(name = "role")
    private String role;

    @Column(name = "phone")
    private String phone;


    @Column(name = "raw_name")
    private String rawName;

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

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureOffering> offerings = new ArrayList<>();

}
