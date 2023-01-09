package me.sonam.jwt;

import me.sonam.security.jwt.JwtBody;
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
    public Mono<String> create(Mono<JwtBody> jwtBodyMono) {
        LOG.info("create jwt token using the body");

        return jwtBodyMono.flatMap(jwtBody -> jwtCreator.create(jwtBody));

    }

    @Override
    public Mono<String> getPublicKey(UUID keyId) {
        LOG.info("get public key for keyId: {}", keyId);

        return jwtCreator.getPublicKey(keyId);
    }

}
