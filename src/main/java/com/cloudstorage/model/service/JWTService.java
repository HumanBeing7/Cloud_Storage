package com.cloudstorage.model.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudstorage.model.security.SecurityConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
    //pill secret key from .env
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; 

    public String generateToken(String email){
        return generateToken( new HashMap<>(),email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email) // WHO this badge belongs to
                .setIssuedAt(new Date(System.currentTimeMillis())) // WHEN it was printed
                // Cleanly using your constants class for the 24-hour expiration!
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) 
                .signWith(getSignInKey(),SignatureAlgorithm.HS256) // The cryptographic signature
                .compact();
    }

    // A helper method to decode your secret key into a secure mathematical format
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
