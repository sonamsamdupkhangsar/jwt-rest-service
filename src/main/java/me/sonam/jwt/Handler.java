package me.sonam.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.sonam.jwt.json.HmacBody;
import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.JwtException;
import me.sonam.security.jwt.PublicKeyJwtCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler
 */
@Component
public class Handler  {
    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

    @Autowired
    private Jwt jwt;

    @Autowired
    private JwtCreator jwtCreator;
    private final String bearer = "Bearer: ";

    public Mono<ServerResponse> createAccessToken(ServerRequest serverRequest) {
        LOG.info("create jwt token");

        return serverRequest.bodyToMono(String.class).flatMap(json -> getJwtBody(json).zipWith(Mono.just(json)))
                .flatMap(objects ->{
                    LOG.info("json body: {}", objects.getT2());
                    if (serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION) == null) {
                        return Mono.error(new JwtException("Hmac digest is missing in the Authorization header"));
                    }
                    final String hmacToken = serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION).replace("Bearer ", "");
                    return jwtCreator.hmacMatches(hmacToken,
                        objects.getT2(), objects.getT1().getClientId()).zipWith(Mono.just(objects.getT1()));
                })
                .flatMap(objects -> {
                    if (objects.getT1() != true) {
                        return Mono.error(new JwtException("hmac digest does not match"));
                    }
                    else {
                        if (objects.getT2().getUserJwt() != null) {
                            LOG.info("use userJwt to create access token");
                            return jwt.create(Mono.just(objects.getT2().getUserJwt()));
                        }
                        else {
                            LOG.info("use client app to create access token");
                            return jwt.create(Mono.just(objects.getT2()));
                        }
                    }
                })
                .flatMap(s -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(getMap(Pair.of("token", s)))
                ).onErrorResume(throwable -> {
                    LOG.error("create jwt token failed", throwable);
                    //LOG.error("create jwt token failed");
                    if (throwable.getMessage().startsWith("Hmac digest is missing")) {
                        LOG.error("hmac digest is missing");
                        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(getMap(Pair.of("error", "Hmac digest is missing in the Authorization header")));
                    }
                    if (throwable.getMessage().equals("hmac digest does not match")) {
                        LOG.error("hmac digest does not match");
                        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(getMap(Pair.of("error", "hmac digest does not match")));
                    }
                    else {
                        LOG.error("failed to create jwt token");
                        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(getMap(Pair.of("error", "failed to create JWT token")));
                    }
                });
    }

    public Mono<ServerResponse> getPublicKey(ServerRequest serverRequest) {
        LOG.info("get public key for keyId");

        return jwt.getPublicKey(UUID.fromString(serverRequest.pathVariable("keyId")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(getMap(Pair.of("key", s))))
                .onErrorResume(throwable -> {
                    LOG.error("get public key failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(getMap(Pair.of("error", "failed to get public key")));
                });
    }

    public Mono<ServerResponse> getKeyId(ServerRequest serverRequest) {
        LOG.info("get keyId from jwt");

        return jwt.getKeyId(serverRequest.bodyToMono(String.class))
                .flatMap(keyId -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(getMap(Pair.of("keyId", keyId)))
                )
                .onErrorResume(throwable -> {
                    LOG.error("get key Id failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(getMap(Pair.of("error", "failed to get keyId")));
                });
    }
    public Mono<ServerResponse> generateHmac(ServerRequest serverRequest) {
        LOG.info("get hmac from json string");

        return jwt.generateHmac(serverRequest.pathVariable("algorithm"),
                        serverRequest.bodyToMono(String.class),
                        serverRequest.pathVariable("key"))
                .flatMap(hmac ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(getMap(Pair.of("hmac", hmac)))
                )
                .onErrorResume(throwable -> {
                    LOG.error("get key Id failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(getMap(Pair.of("error", "failed to generate hmac")));
                });
    }

    public Mono<ServerResponse> createHmacKey(ServerRequest serverRequest) {
        LOG.info("create hmacKey for clientId");

        return jwt.createHmacKey(serverRequest.pathVariable("clientId"))
                .flatMap(hmac ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(hmac)
                )
                .onErrorResume(throwable -> {
                    LOG.error("create hmacKey failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(getMap(Pair.of("error", "failed to create hmacKey")));
                });
    }

    private Map<String, String> getMap(Pair<String, String>... pairs){
        Map<String, String> map = new HashMap<>();

        for(Pair<String, String> pair: pairs) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }

    private Mono<JwtBody> getJwtBody(final String json) {
        LOG.info("convert json to JwtBody");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return Mono.just(objectMapper.readValue(json, JwtBody.class));
        }
        catch(Exception e) {
            LOG.error("failed to conver json to JwtBody", e.getMessage());
            return Mono.error(new JwtException("failed to convert json to JwtBody"));
        }

    }
}
