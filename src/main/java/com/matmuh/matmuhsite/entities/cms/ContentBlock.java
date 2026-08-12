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
        name = "content_blocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_content_block_address",
                columnNames = {"slug", "block_path", "locale"}
        )
)
public class ContentBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "block_path", nullable = false)
    private String blockPath;


    @Column(name = "locale", length = 16)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", length = 20, nullable = false)
    private BlockType blockType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb", nullable = false)
    private JsonNode value;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "item_schema", columnDefinition = "jsonb")
    private JsonNode itemSchema;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

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
