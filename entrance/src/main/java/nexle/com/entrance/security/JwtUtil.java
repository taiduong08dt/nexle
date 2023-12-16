package nexle.com.entrance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import nexle.com.entrance.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    String secret;

    @Value("${jwt.expirationDateInMs}")
    private long duration;
    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationTime; // 30 days

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + duration))
                .signWith(getKey(), SignatureAlgorithm.HS256).compact();

    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime * 24 * 60 * 60 * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS256).compact();

    }
    public String parseEmail(String jwt) {
        return getClaims(jwt).getSubject();
    }
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        String username = parseEmail(jwt);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }
    public boolean isTokenExpired(String jwt) {
        Date date = getClaims(jwt).getExpiration();
        return date.before(new Date());
    }
    public Claims getClaims(String jwt) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
