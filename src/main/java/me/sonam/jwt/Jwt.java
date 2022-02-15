package me.sonam.jwt;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface Jwt {
    Mono<String> create(String subject, String audience, int calendarField, int calendarValue);
    Mono<Map<String, String>> validate(String jwt);
}
