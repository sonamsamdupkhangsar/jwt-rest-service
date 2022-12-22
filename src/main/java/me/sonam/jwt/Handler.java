package me.sonam.jwt;

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

    public Mono<ServerResponse> createJwt(ServerRequest serverRequest) {
        String clientUserRole = serverRequest.pathVariable("clientUserRole");
        String clientId = serverRequest.pathVariable("clientId");
        String groupNames = serverRequest.pathVariable("groupNames");
        String username = serverRequest.pathVariable("username");
        String audience = serverRequest.pathVariable("audience");
        String expireField = serverRequest.pathVariable("expireField");
        String expireIn = serverRequest.pathVariable("expireIn");


        LOG.info("generating jwt");

        return jwt.create(clientUserRole, clientId, groupNames, username, audience, Integer.parseInt(expireField), Integer.parseInt(expireIn))
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
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get public key failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }
}
