package me.kverna.roger.server.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Properties for handling of JSON web tokens.
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * A secret for the JSON web tokens. If defined, this must be
     * greater than or equal to 256 bits. When the secret is null,
     * the secretAlgorithm will be used.
     */
    @Getter @Setter private String secret = null;

    /**
     * The secret algorithm to use when the secret is null. A secret
     * will be generated using the given algorithm.
     */
    @Getter @Setter private String secretAlgorithm = "HS512";

    /**
     * How many days a key will be valid for until expiring.
     */
    @Getter @Setter private long expirationDays = 7;
}
