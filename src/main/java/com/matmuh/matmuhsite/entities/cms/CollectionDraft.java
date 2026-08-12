package com.matmuh.matmuhsite.entities.cms;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "collection_drafts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_collection_draft_owner",
                columnNames = {"collection_key", "slug", "user_id", "is_new", "locale"}
        )
)
public class CollectionDraft {

    public static final String DEFAULT_SLUG = "";


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "collection_key", length = 100, nullable = false)
    private String collectionKey;

    @Column(name = "slug", nullable = false)
    @Builder.Default
    private String slug = DEFAULT_SLUG;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;


    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "is_new", nullable = false)
    @Builder.Default
    private boolean forNewItem = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;








}