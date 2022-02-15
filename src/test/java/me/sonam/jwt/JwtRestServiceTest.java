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

        client.get().uri("/hello").exchange().expectStatus().isOk();
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


}
