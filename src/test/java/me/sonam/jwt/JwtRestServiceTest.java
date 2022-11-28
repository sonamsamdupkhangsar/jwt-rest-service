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
import java.util.UUID;

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
        final String clientId = UUID.randomUUID().toString();
        final String groups = "Admin, Cameramen, Driver, foodballer";

        final String path = "/create/"+clientId+"/"+groups+"/" + subject + "/" + audience + "/" + expireInField + "/" + expireIn;

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
}
