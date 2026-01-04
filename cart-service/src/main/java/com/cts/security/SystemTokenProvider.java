package com.cts.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class SystemTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Generates a token that identifies this request as coming from "System" with ADMIN role
    public String generateSystemToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 300000); // 5 minutes validity is enough for internal calls

        return Jwts.builder()
                .subject("system-cart-service") // Subject is the service, not a user
                .claim("roles", List.of("ROLE_SYSTEM", "SYSTEM")) // Grant Admin Roles
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}