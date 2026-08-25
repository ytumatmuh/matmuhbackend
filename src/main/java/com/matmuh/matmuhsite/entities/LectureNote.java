package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE lecture_notes SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "lecture_notes")
public class LectureNote extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;


    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "view_count")
    private int viewCount = 0;


    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32)
    private NoteType type = NoteType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 16)
    private NoteReviewStatus status = NoteReviewStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_offering_id")
    private LectureOffering lectureOffering;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", referencedColumnName = "id", unique = false)
    private File file;


}
