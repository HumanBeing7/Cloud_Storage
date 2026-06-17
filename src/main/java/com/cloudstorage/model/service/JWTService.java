package com.cloudstorage.model.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
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

    //EXTRACTOR METHOD TO EXTRACT CREDENTIALS
    //Extract the email
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //extract any piece of data from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    //Check Signature against secret key
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())   //get the secret key of our-> prevent tampering of our key
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    //VALIDATOR -> CHECK TOKEN
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        // Valid if: emails match AND token is NOT expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    //Helper Method: check the expiration 
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Returns TRUE if the token is dead
    }

    //Get Date in token
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(String email) {
        return generateToken(new HashMap<>(), email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email) // WHO this badge belongs to
                .setIssuedAt(new Date(System.currentTimeMillis())) // WHEN it was printed
                // Cleanly using your constants class for the 24-hour expiration!
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // The cryptographic signature
                .compact();
    }

    // A helper method to decode your secret key into a secure mathematical format
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
