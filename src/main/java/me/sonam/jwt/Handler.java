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

/**
 * Handler
 */
@Component
public class Handler  {
    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

    @Autowired
    private Jwt jwt;

    private final String bearer = "Bearer: ";

    /**
     * outline only
     */
    public Mono<ServerResponse> createJwt(ServerRequest serverRequest) {
        String username = serverRequest.pathVariable("username");
        String audience = serverRequest.pathVariable("audience");
        String expireField = serverRequest.pathVariable("expireField");
        String expireIn = serverRequest.pathVariable("expireIn");

        LOG.info("generating jwt");

        return jwt.create(username, audience, Integer.parseInt(expireField), Integer.parseInt(expireIn))
                .flatMap(s -> {
                Map<String, String> map = new HashMap<>();
                map.put("token", s);
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(map);}
                                );
    }

    public Mono<ServerResponse> validate(ServerRequest serverRequest) {
        return jwt.validate(serverRequest.pathVariable("jwt"))
                .flatMap(map -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(map));

    }

    public Mono<ServerResponse> validateHeader(ServerRequest serverRequest) {
        LOG.info("processing jwt from header: {}", serverRequest.headers().firstHeader("Authorization"));
        return jwt.validate(serverRequest.headers().firstHeader("Authorization").replace(bearer, ""))
                .flatMap(map -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(map));

    }
}
