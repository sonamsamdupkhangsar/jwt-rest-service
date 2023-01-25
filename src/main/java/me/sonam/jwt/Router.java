package me.sonam.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Set Email route
 */
@Configuration
public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Bean
    public RouterFunction<ServerResponse> route(Handler handler) {
        LOG.info("building router function");

        return RouterFunctions.route(POST("/jwts/accesstoken").
                        and(accept(MediaType.APPLICATION_JSON)), handler::createAccessToken)
                .andRoute(POST("/jwts/keyId"). and(accept(MediaType.APPLICATION_JSON)), handler::getKeyId)
                .andRoute(GET("/jwts/publickeys/{keyId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getPublicKey)
                .andRoute(POST("/jwts/hmac/{algorithm}/{key}").and(accept(MediaType.APPLICATION_JSON)), handler::generateHmac)
                .andRoute(POST("/jwts/hmackey/{clientId}").and(accept(MediaType.APPLICATION_JSON)), handler::createHmacKey);

    }
}
