package me.sonam.jwt;

import me.sonam.security.jwt.JwtCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class JwtService implements Jwt {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Autowired
    private JwtCreator jwtCreator;

    static final String TOKEN_PREFIX = "Bearer";

    public JwtService() {
    }

    @Override
    public Mono<String> create(String clientId, String groupNames, String subject, String audience, int calendarField, int calendarValue) {
        LOG.info("issuer: {}, secret: {}", issuer, secret);

        return jwtCreator.create(clientId, groupNames, subject, audience, calendarField, calendarValue);
    }

    @Override
    public Mono<String> getPublicKey(UUID keyId) {
        LOG.info("get public key for keyId");

        return jwtCreator.getPublicKey(keyId);
    }

}
