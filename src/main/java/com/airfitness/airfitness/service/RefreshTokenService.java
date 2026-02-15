package com.airfitness.airfitness.service;

import com.airfitness.airfitness.common.exception.UnauthorizedException;
import com.airfitness.airfitness.entity.Organization;
import com.airfitness.airfitness.entity.RefreshToken;
import com.airfitness.airfitness.entity.User;
import com.airfitness.airfitness.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private RefreshTokenRepository repo;
    private PasswordEncoder passwordEncoder;

    public String create(User user, Organization organization) {
        String raw = UUID.randomUUID().toString() + "." +  UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setOrganization(organization);
        refreshToken.setTokenHash(raw);
        refreshToken.setCreatedAt(Instant.now().plus(7, ChronoUnit.DAYS));

        repo.save(refreshToken);
        return raw;
    }

    public RefreshToken validate(Long userId, String raw) {
        List<RefreshToken> tokens = repo.findByUserIdAndRevokedFalse(userId);

        return tokens.stream()
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .filter(t -> passwordEncoder.matches(raw, t.getTokenHash()))
                .findFirst()
                .orElseThrow(UnauthorizedException::new);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repo.save(token);
    }

    @Transactional
    public void revokeByRaw(String raw) {
        List<RefreshToken> tokens = repo.findAll();

        for (RefreshToken t : tokens) {
            if (passwordEncoder.matches(raw, t.getTokenHash())) {
                t.setRevoked(true);
                repo.save(t);
                return;
            }
        }

        throw new UnauthorizedException("Invalid refresh token");
    }
}
