package com.trackit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:trackit-super-secret-key-designed-for-local-development-fallback-only-32bytes}")
    private String secret;

    @Value("${jwt.expiration:86400}") // Default 24 hours in seconds
    private long expirationSeconds;

    private static final String ISSUER = "trackit";
    private static final String CLAIM_USERNAME = "username";

    public String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim(CLAIM_USERNAME, username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expirationSeconds * 1000)))
                .sign(algorithm);
    }

    public String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim(CLAIM_USERNAME).asString();
    }
}
