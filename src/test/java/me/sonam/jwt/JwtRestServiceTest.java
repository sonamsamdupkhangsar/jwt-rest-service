package me.sonam.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sonam.security.jwt.JwtBody;
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

import java.time.Duration;
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
        final String clientId = "sonam-123-322";
        final String subject = UUID.randomUUID().toString();
        final String audience = "email"; //the resource to access
        final String scopes = "email.write";
        final String role = "user";
        final String groups = "email, messaging";

        JwtBody jwtBody = new JwtBody(subject, scopes, clientId, audience, role, groups, 10);

        EntityExchangeResult<Map> entityExchangeResult = client.post().uri("/jwts/accesstoken")
                .bodyValue(jwtBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult();

        Map<String, String> map = entityExchangeResult.getResponseBody();
        LOG.info("assert token not empty");
        assertThat(map.get("token")).isNotNull();
        LOG.info("token: {}", map.get("token"));
        UUID keyId = getKeyId(map.get("token").toString());
        LOG.info("keyId: {}", keyId);

        getRestApiKeyId(map.get("token"));

        getPublicKey(keyId);
    }

    private void getRestApiKeyId(String jwt) {
        EntityExchangeResult<Map> entityExchangeResult = client.post().uri("/jwts/keyId")
                .bodyValue(jwt)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult();

        Map<String, String> map = entityExchangeResult.getResponseBody();
        LOG.info("assert keyId not empty");
        assertThat(map.get("keyId")).isNotNull();
        LOG.info("keyId: {}",map.get("keyId"));
    }
    public void getPublicKey(UUID keyId) {
        final String path = "/jwts/publickeys/" + keyId;

        LOG.info("get public key");
        EntityExchangeResult<String> result = client.get().uri(path)
                .exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("publicKey: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isNotNull();

    }

    public UUID getKeyId(String jwtToken) {
        LOG.debug("getKeyId for jwtToken by marshaling string to SonamsJwtHeader class");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String[] chunks = jwtToken.split("\\.");

        String header = new String(decoder.decode(chunks[0]));
        final String payload = new String(decoder.decode(chunks[1]));
        LOG.debug("header: {}", header);
        LOG.info("payload: {}", payload);

        ObjectMapper mapper = new ObjectMapper();

        try {
            JwtBody jwtBody = mapper.readValue(payload, JwtBody.class);
            LOG.debug("returning keyId: {}", jwtBody.getKeyId());
            return jwtBody.getKeyId();
        } catch (JsonProcessingException e) {
            LOG.error("failed to convert header to sonams jwt header", e);
            return null;
        }

    }
}
