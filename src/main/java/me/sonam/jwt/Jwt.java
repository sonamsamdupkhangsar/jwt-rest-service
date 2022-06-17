package me.sonam.jwt;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface Jwt {
    Mono<String> create(String subject, String audience, int calendarField, int calendarValue, String apiKey);
    Mono<Map<String, String>> validate(String jwt);
}
