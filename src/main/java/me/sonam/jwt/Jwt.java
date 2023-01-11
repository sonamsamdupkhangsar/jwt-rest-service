package me.sonam.jwt;

import me.sonam.security.jwt.JwtBody;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface Jwt {
    Mono<String> create(Mono<JwtBody> jwtBodyMono);
    Mono<String> getPublicKey(UUID keyId);
    Mono<String> getKeyId(Mono<String> jwt);
}
