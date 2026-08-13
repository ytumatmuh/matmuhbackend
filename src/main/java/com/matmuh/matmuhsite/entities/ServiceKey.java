package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "service_keys",
        uniqueConstraints = @UniqueConstraint(name = "uk_service_key_prefix", columnNames = "key_prefix"),
        indexes = @Index(name = "ix_service_key_client", columnList = "client_key")
)
public class ServiceKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @Column(name = "client_key", length = 100, nullable = false)
    private String clientKey;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "key_prefix", length = 40, nullable = false)
    private String keyPrefix;

    @Column(name = "key_hash", length = 64, nullable = false)
    private String keyHash;

    @ElementCollection(targetClass = ServiceKeyCapability.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "service_key_capabilities", joinColumns = @JoinColumn(name = "service_key_id"))
    @Column(name = "capability", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<ServiceKeyCapability> capabilities = new HashSet<>();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isActive(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
}
