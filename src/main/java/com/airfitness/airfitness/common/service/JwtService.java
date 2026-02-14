package com.airfitness.airfitness.common.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    /*
    TODO
     */
    private final String SECRET = "secret";

    public String generateAccessToken(Long userId, Long orgId, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("org", orgId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}

