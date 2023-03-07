package me.sonam.jwt;

import me.sonam.jwt.json.HmacBody;
import me.sonam.security.jwt.JwtBody;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface Jwt {
    Mono<String> create(Mono<JwtBody> jwtBodyMono);
    Mono<String> getPublicKey(UUID keyId);
    Mono<String> getKeyId(Mono<String> jwt);
    Mono<String> generateHmac(final String algoirthm, Mono<String> monoData, final String key);
    Mono<String> generateHmac(Mono<HmacBody> hmacBody);
    Mono<String> createHmacKey(String clientId);
}
