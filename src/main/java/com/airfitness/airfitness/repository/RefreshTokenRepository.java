package com.airfitness.airfitness.repository;

import com.airfitness.airfitness.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
}