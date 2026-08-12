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
        name = "collection_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_collection_item_slug",
                columnNames = {"collection_key", "slug"}
        ),
        indexes = @Index(name = "ix_collection_item_key", columnList = "collection_key")
)
public class CollectionItem {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "collection_key", length = 100, nullable = false)
    private String collectionKey;

    @Column(name = "slug", nullable = false)
    private String slug;


    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "translation_group_id")
    private UUID translationGroupId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb", nullable = false)
    private JsonNode data;

    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private boolean archived = false;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}