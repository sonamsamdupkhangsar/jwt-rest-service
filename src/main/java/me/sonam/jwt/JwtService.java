package me.sonam.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sonam.jwt.json.HmacBody;
import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.JwtException;
import me.sonam.security.jwt.PublicKeyJwtCreator;
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
        return jwtMono.flatMap(jwt -> jwtCreator.getKeyId(jwt));
    }

    @Override
    public Mono<String> generateHmac(final String algoirthm, Mono<String> monoData, final String key) {
        LOG.info("generate hmac");
        return monoData.flatMap(data ->
        Mono.just(PublicKeyJwtCreator.getHmac(algoirthm, data, key)));
    }

    @Override
    public Mono<String> generateHmac(Mono<HmacBody> hmacBodyMono) {
        LOG.info("generate hmac2 ");
        return hmacBodyMono.flatMap(hmacBody -> {
            LOG.info("hmacBody: {}", hmacBody);
                 return Mono.just(PublicKeyJwtCreator.getHmac(hmacBody.getAlgorithm(), hmacBody.getData(), hmacBody.getKey()));});
    }

}
