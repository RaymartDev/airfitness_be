package com.airfitness.airfitness.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrganizationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne User user;
    @ManyToOne Organization organization;

    private String role;
}
