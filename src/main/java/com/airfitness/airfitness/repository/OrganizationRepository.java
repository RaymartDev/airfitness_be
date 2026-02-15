package com.airfitness.airfitness.repository;

import com.airfitness.airfitness.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
