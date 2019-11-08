package me.kverna.roger.server.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Handles signing and verification of JSON web tokens. This component is
 * instantiated with a secret key loaded using the secret defined in the
 * JwtProperties, or generated using the specified secret algorithm.
 */
@Log
@Component
public class JwtManager {

    private JwtProperties jwtProperties;
    private Key key;

    @Autowired
    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        // Create a new unique secret unless a secret is specified in the configuration
        if (jwtProperties.getSecret() == null) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.forName(jwtProperties.getSecretAlgorithm()));
            log.info("Generated new JWT secret with algorithm " + key.getAlgorithm());
        } else {
            this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            log.info("Loading JWT secret from configuration using algorithm " + key.getAlgorithm());
        }
    }

    /**
     * Returns a new expiration date. The days until expiration should be
     * defined in the properties.
     *
     * @return a new expiration date
     */
    private Date createExpiration() {
        return new Date(System.currentTimeMillis() + jwtProperties.getExpirationDays() * 86_400_000);
    }

    /**
     * Generates a signed JSON web token using the given subject. The token
     * will have an expiration date as per createExpiration.
     *
     * @param subject the subject to add to the token
     * @return a signed JSON web token
     */
    public String generateToken(String subject) {
        return Jwts.builder().setSubject(subject).signWith(key).setExpiration(createExpiration()).setIssuedAt(new Date()).compact();
    }

    /**
     * Verify a JSON web token and return the subject if it is valid and has not expired.
     *
     * @param token the JSON web token to verify
     * @return the subject if the token is valid or null
     */
    public String getSubject(String token) {
        try {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
        } catch (SignatureException | ExpiredJwtException e) {
            return null;
        }
    }
}
