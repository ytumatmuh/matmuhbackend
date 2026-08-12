package com.matmuh.matmuhsite.core.security;


import com.matmuh.matmuhsite.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access-validity-seconds:3600}")
    private long tokenValiditySeconds;

    public long getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    public String generateToken(User user){
        Map<String, Object> claims = new HashMap<>();

        var authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("name", fullName(user));
        claims.put("email", user.getEmail());
        claims.put("department", user.getDepartment());
        claims.put("authorities", authorities);

        return createToken(claims, user.getUsername());
    }

    private String fullName(User user) {
        return Stream.of(user.getFirstName(), user.getLastName())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        String email = extractUser(token);
        Date expirationDate = extractExpiration(token);
        return userDetails.getUsername().equals(email) && !expirationDate.before(new Date());
    }
    private Date extractExpiration(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }
    public String extractUser(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    private String createToken(Map<String, Object> claims, String email) {
        var result = Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenValiditySeconds * 1000L))
                .signWith(getSignKey())
                .compact();
        return result;
    }
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

