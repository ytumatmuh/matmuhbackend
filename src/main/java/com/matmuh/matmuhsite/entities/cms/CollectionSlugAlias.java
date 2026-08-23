package com.matmuh.matmuhsite.entities.cms;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "collection_slug_aliases",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_collection_slug_alias",
                columnNames = {"collection_key", "slug"}
        ),
        indexes = @Index(name = "ix_collection_slug_alias_item", columnList = "item_id")
)
public class CollectionSlugAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "collection_key", length = 100, nullable = false)
    private String collectionKey;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
