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

    @Autowired
    private JwtCreator jwtCreator;

    public JwtService() {
    }

    @Override
    public Mono<String> create(String clientUserRole, String clientId, String groupNames, String subject, String audience, int calendarField, int calendarValue) {
        LOG.info("create jwt");

        return jwtCreator.create(clientUserRole, clientId, groupNames, subject, audience, calendarField, calendarValue);
    }

    @Override
    public Mono<String> getPublicKey(UUID keyId) {
        LOG.info("get public key for keyId");

        return jwtCreator.getPublicKey(keyId);
    }

}
