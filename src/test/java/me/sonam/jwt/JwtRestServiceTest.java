package me.sonam.jwt;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtRestServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(JwtRestServiceTest.class);

    @Autowired
    private WebTestClient client;

    @Value("${jwt.issuer}")
    private String issuer;

    @Test
    public void getJwt() {
        final String audience = "sonam.cloud";
        final String subject = "sonam";
        final int expireInField = 5;
        final int expireIn = 10;

        final String path = "/create/" + subject + "/" + audience + "/" + expireInField + "/" + expireIn;

        FluxExchangeResult<Map> fluxExchangeResult = client.get().uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .returnResult(Map.class);
        StepVerifier.create(fluxExchangeResult.getResponseBody())
                .assertNext(map -> {
                    LOG.info("assert token not empty");
                    assertThat(map.get("token")).isNotNull();
                }).verifyComplete();
    }

    @Test
    public void validateJwt() {
        final String audience = "sonam.cloud";
        final String subject = "sonam";
        final int expireInField = 5;
        final int expireIn = 10;

        final String path = "/create/" + subject + "/" + audience + "/" + expireInField + "/" + expireIn;

        client.get().uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectBody(Map.class)
                .value(s -> {
                    LOG.info("value: {}", s.get("token"));

                    FluxExchangeResult<Map> fluxExchangeResult = client.get().uri("/validate/" + s.get("token"))
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange().expectStatus().isOk()
                            .returnResult(Map.class);

                    StepVerifier.create(fluxExchangeResult.getResponseBody())
                            .assertNext(map -> {
                                LOG.info("audience: {}", map.get("audience"));
                                assertThat(map.get("audience")).isEqualTo(audience);

                                LOG.info("issuer: {}", map.get("issuer"));
                                assertThat(map.get("issuer")).isEqualTo(issuer);

                                LOG.info("id: {}", map.get("id"));
                                assertThat(map.get("id")).isNotNull();

                                LOG.info("issuer: {}", map.get("issuer"));
                                assertThat(map.get("subject")).isEqualTo(subject);
                                LOG.info("verfied claims");
                            }).verifyComplete();

                    LOG.info("set jwt header for Authorization bearer token");
                    fluxExchangeResult = client.get().uri("/validate")
                            .headers(httpHeaders -> httpHeaders.setBearerAuth(s.get("token").toString()))
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange().expectStatus().isOk()
                            .returnResult(Map.class);

                    StepVerifier.create(fluxExchangeResult.getResponseBody())
                            .assertNext(map -> {
                                LOG.info("audience: {}", map.get("audience"));
                                assertThat(map.get("audience")).isEqualTo(audience);

                                LOG.info("issuer: {}", map.get("issuer"));
                                assertThat(map.get("issuer")).isEqualTo(issuer);

                                LOG.info("id: {}", map.get("id"));
                                assertThat(map.get("id")).isNotNull();

                                LOG.info("issuer: {}", map.get("issuer"));
                                assertThat(map.get("subject")).isEqualTo(subject);
                                LOG.info("verfied claims");
                            }).verifyComplete();
                });
    }

    @Test
    public void badSignature() {
        final String jwt= "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJz25hbSIsImlzcyI6InNvbmFtLmNsb3VkIiwiYXVkIjoic29uYW0uY2xvdWQiLCJqdGkiOiJmMTY2NjM1OS05YTViLTQ3NzMtOWUyNy00OGU0OTFlNDYzNGIifQ.KGFBUjghvcmNGDH0eM17S9pWkoLwbvDaDBGAx2AyB41yZ_8-WewTriR08JdjLskw1dsRYpMh9idxQ4BS6xmOCQ";

        FluxExchangeResult<String> fluxExchangeResult = client.get().uri("/validate")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isUnauthorized()
                .returnResult(String.class);

        StepVerifier.create(fluxExchangeResult.getResponseBody())
                .assertNext(s -> {
                    LOG.info("data: {}", s);
                    assertThat(s).isEqualTo("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
                }).verifyComplete();
    }

    @Test
    public void expiredJwt() {
        final String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzb25hbSIsImlzcyI6InNvbmFtLmNsb3VkIiwiYXVkIjoic29uYW0uY2xvdWQiLCJleHAiOjE2NTY0NTQ2NDQsImp0aSI6ImJmNjI4OTI1LTIzN2EtNDRlNy1hYjlmLTAwYTVjZmRmYzVkNSJ9.BafIk8NcNuR7YhJNe1BabDctzutlWkPM47EW3umCEaEXhrcXoKsT__daVpFkVru2Y-oXFbRwv7I4hJxlXWZK1A";

        FluxExchangeResult<String> fluxExchangeResult = client.get().uri("/validate")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isUnauthorized()
                .returnResult(String.class);

        StepVerifier.create(fluxExchangeResult.getResponseBody())
                .assertNext(s -> {
                    LOG.info("data: {}", s);
                    assertThat(s).startsWith("JWT expired at ");
                }).verifyComplete();
    }
}
