package me.sonam.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Base64;
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

    @Override
    public Mono<String> getKeyId(Mono<String> jwtMono) {
        LOG.info("get keyId from jwt");
        ObjectMapper objectMapper = new ObjectMapper();

        return jwtMono.flatMap(jwt -> {
            try {
                Base64.Decoder decoder = Base64.getUrlDecoder();

                String[] chunks = jwt.split("\\.");
                if (chunks.length >= 2) {
                    final String payload = new String(decoder.decode(chunks[1]));

                    JwtBody jwtBody = objectMapper.readValue(payload, JwtBody.class);
                    return Mono.just(jwtBody);
                }
                else {
                    return Mono.error(new me.sonam.jwt.JwtException("jwt is invalid, jwt split is less than 2"));
                }
            } catch (JsonProcessingException e) {
                LOG.error("failed to marshal to jwtBody", e);
                return Mono.error(new JwtException("Failed to convert the jwt token to get keyId, error: "+ e.getMessage()));
            }
        }).map(jwtBody -> jwtBody.getKeyId().toString());
    }
}
