package com.example.career.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public String generateToken(String subject) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured (property 'jwt.secret')");
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndGetSubject(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException ex) {
            // token invalid or expired
            return null;
        }
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
