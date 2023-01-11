package me.sonam.jwt;

import me.sonam.security.jwt.JwtBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final String bearer = "Bearer: ";

    public Mono<ServerResponse> createAccessToken(ServerRequest serverRequest) {
        LOG.info("create jwt token");

        return jwt.create(serverRequest.bodyToMono(JwtBody.class))
                .flatMap(s -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("token", s);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(map);}
                );
    }

    public Mono<ServerResponse> getPublicKey(ServerRequest serverRequest) {
        LOG.info("get public key for keyId");

        return jwt.getPublicKey(UUID.fromString(serverRequest.pathVariable("keyId")))
                .flatMap(s -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("publicKey", s);
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(map);
                    }
                )
                .onErrorResume(throwable -> {
                    LOG.error("get public key failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> getKeyId(ServerRequest serverRequest) {
        LOG.info("get keyId from jwt");

        return jwt.getKeyId(serverRequest.bodyToMono(String.class))
                .flatMap(keyId -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("keyId", keyId);
                            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(map);
                        }
                )
                .onErrorResume(throwable -> {
                    LOG.error("get key Id failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }
}
