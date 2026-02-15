package com.airfitness.airfitness.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    /*
    TODO
     */
    private final String SECRET = "vtJUH+z1+c8qlCVbAESEuhFh/5HGiOdty4l5dnE//S8=";

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

    public Claims parseExpired(String token) {
        try {
            return parse(token);
        } catch (ExpiredJwtException ex) {
            return ex.getClaims(); // still trustworthy
        }
    }
}

