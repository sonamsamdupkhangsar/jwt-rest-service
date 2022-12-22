package me.sonam.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Base64;
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
        final String clientUserRole = "admin";

        final String path = "/jwt-rest-service/create/" + clientUserRole + "/" + clientId + "/" + groups + "/" + subject + "/" + audience + "/" + expireInField + "/" + expireIn;

        FluxExchangeResult<Map> fluxExchangeResult = client.get().uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .returnResult(Map.class);

        StepVerifier.create(fluxExchangeResult.getResponseBody())
                .assertNext(map -> {
                    LOG.info("assert token not empty");
                    assertThat(map.get("token")).isNotNull();
                    LOG.info("token: {}", map.get("token"));
                    UUID keyId = getKeyId(map.get("token").toString());
                    LOG.info("keyId: {}", keyId);
                }).verifyComplete();
    }

  //  @Test
    public void getPublicKeyTest() {
        final UUID keyId = UUID.fromString("f88369b-b86d-4e5d-a9eb-fcd9261fa61c");
        getPublicKey(keyId);
    }

    public void getPublicKey(UUID keyId) {
        final String path = "http://localhost:8081/jwt-rest-service/publickeys/" + keyId;

        LOG.info("get public key");
        EntityExchangeResult<String> result = client.get().uri(path)
                .exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("publicKey: {}", result.getResponseBody());

        LOG.info("response: {}", result.getResponseBody());

        //return Mono.just(result.getResponseBody().get("publicKey").toString());
/*
                .exchange().expectStatus().isOk()
                .returnResult(Map.class).getResponseBody()
                .single().map(map -> map.get("publicKey").toString())
                .flatMap(o -> Mono.just(o.toString()));
*/
    }

    public UUID getKeyId(String jwtToken) {
        LOG.debug("getKeyId for jwtToken by marshaling string to SonamsJwtHeader class");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String[] chunks = jwtToken.split("\\.");

        String header = new String(decoder.decode(chunks[0]));
        LOG.debug("header: {}", header);

        ObjectMapper mapper = new ObjectMapper();

        try {
            SonamsJwtHeader sonamsJwtHeader = mapper.readValue(header, SonamsJwtHeader.class);
            LOG.debug("returning keyId: {}", sonamsJwtHeader.getKeyId());
            return sonamsJwtHeader.getKeyId();
        } catch (JsonProcessingException e) {
            LOG.error("failed to convert header to sonams jwt header", e);
            return null;
        }

    }
}
