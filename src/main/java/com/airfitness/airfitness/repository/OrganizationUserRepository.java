package com.airfitness.airfitness.repository;

import com.airfitness.airfitness.entity.Organization;
import com.airfitness.airfitness.entity.OrganizationUser;
import com.airfitness.airfitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationUserRepository extends JpaRepository<OrganizationUser, Long> {
    Optional<OrganizationUser> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    Optional<OrganizationUser> findFirstByUser(User user);
    Optional<OrganizationUser> findFirstByUserId(Long userId);
}
