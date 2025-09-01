package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final long TOKEN_EXPIRATION_TIME = 7200000L;
    private static final String ISSUER = "ecommerce-api";

    public String generateToken(User user) {
        try {
            return Jwts.builder()
                    .issuer(ISSUER)
                    .subject(user.getLogin())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido", e);
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}