package com.airfitness.airfitness.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Organization organization;

    private String tokenHash;
    private Instant expiresAt;
    private boolean revoked = false;
    private Instant createdAt = Instant.now();
}
