package me.sonam.jwt;


import au.com.dius.pact.provider.spring.junit5.WebFluxTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sonam.Application;
import me.sonam.jwt.json.HmacBody;
import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.PublicKeyJwtCreator;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
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
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public class JwtRestServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(JwtRestServiceTest.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private JwtCreator jwtCreator;

    @Value("${jwt.issuer}")
    private String issuer;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getJwt() {
        final String clientId = "azudp31223";
        final String subject = UUID.randomUUID().toString();
        final String audience = "email"; //the resource to access
        final String scopes = "email.write";
        final String role = "user";
        final String groups = "email, messaging";
        final String secretKey = "mysecret";

        final String json = "{\n" +
                "  \"sub\": \"01947sxd184\",\n" +
                "  \"scope\": \"authentication\",\n" +
                "  \"clientId\": \"azudp31223\",\n" +
                "  \"aud\": \"backend\",\n" +
                "  \"role\": \"user\",\n" +
                "  \"groups\": \"email, manager\",\n" +
                "  \"expiresInSeconds\": 300\n" +
                "}\n";

        jwtCreator.generateKey(clientId, secretKey).subscribe(hmacKey1 -> LOG.info("crate a HmacKey: {}", hmacKey1));

        final String hmac = PublicKeyJwtCreator.getHmac(PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name(), json, secretKey);

        EntityExchangeResult<Map> entityExchangeResult = client.post().uri("/jwts/accesstoken")
                .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, hmac))
                .bodyValue(json)
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

    @Test
    public void getJwtBadHmac() {
        final String clientId = "azudp31223";
        final String secretKey = "mysecret";

        final String originalJson = "{\n" +
                "  \"sub\": \"01947sxd184\",\n" +
                "  \"scope\": \"authentication\",\n" +
                "  \"clientId\": \"azudp31223\",\n" +
                "  \"aud\": \"backend\",\n" +
                "  \"role\": \"user\",\n" +
                "  \"groups\": \"email, manager\",\n" +
                "  \"expiresInSeconds\": 300\n" +
                "}";

        final String modifiedJson = "{\n" +
                "  \"sub\": \"01947sxd184\",\n" +
                "  \"scope\": \"authentication\",\n" +
                "  \"clientId\": \"azudp31223\",\n" +
                "  \"aud\": \"backend\",\n" +
                "  \"role\": \"user\",\n" +
                "  \"groups\": \"email, manager\",\n" +
                "  \"expiresInSeconds\": 300\n" +
                "}\n";

        jwtCreator.generateKey(clientId, secretKey).subscribe(hmacKey1 -> LOG.info("crate a HmacKey: {}", hmacKey1));

        final String hmac = PublicKeyJwtCreator.getHmac(PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name(), originalJson, secretKey);

        EntityExchangeResult<Map> entityExchangeResult = client.post().uri("/jwts/accesstoken")
                .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, hmac+"x"))
                .bodyValue(modifiedJson)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(Map.class).returnResult();

        Map<String, String> map = entityExchangeResult.getResponseBody();
        assertThat(map.get("error")).isEqualTo("hmac digest does not match");
    }

    @Test
    public void createHmacKey() {
        LOG.info("creat hmac");
        final String authId = "createHmacAuthId";

        org.springframework.security.oauth2.jwt.Jwt jwt = jwt(authId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));


        EntityExchangeResult<Map> result = client.post().uri("/jwts/hmackey/1234-client").
                headers(addJwt(jwt)).exchange().expectStatus().isOk().expectBody(Map.class).returnResult();
        LOG.info("result: {}", result.getResponseBody());

        assertThat(result.getResponseBody().get("hmacMD5Algorithm")).isEqualTo(PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name());
        assertThat(result.getResponseBody().get("clientId")).isEqualTo("1234-client");
        assertThat(result.getResponseBody().get("secretKey")).isNotNull();

        LOG.info("assert we get a bad request or unauthorized without jwt header");
        client.post().uri("/jwts/hmackey/1234-client").
                exchange().expectStatus().isUnauthorized();
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
        EntityExchangeResult<Map> result = client.get().uri(path)
                .exchange()
                .expectStatus().isOk().expectBody(Map.class).returnResult();
        LOG.info("publicKey: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isNotNull();


    }

    @Test
    public void getHmac() {
        LOG.info("generate hmac");
        final String data = "{" +
                "  \"sub\": \"01947sxd184\"," +
                "  \"scope\": \"authentication\"," +
                "  \"clientId\": \"azudp31223\"," +
                "  \"aud\": \"backend\"," +
                "  \"role\": \"user\"," +
                "  \"groups\": \"email, manager\"," +
                "  \"expiresInSeconds\": 300" +
                "}";
        //final String jwtBody = "{\"keyId\":null,\"sub\":\"01947sxd184\",\"scope\":\"authentication\",\"clientId\":\"azudp31223\",\"aud\":\"backend\",\"expiresInSeconds\":300,\"exp\":null,\"iat\":null,\"jti\":null,\"iss\":null,\"role\":\"user\",\"groups\":\"email, manager\"}";;
        //HmacBody hmacBody = new HmacBody(PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name(), jwtBody, "secretkey");
        //LOG.info("json for hmacBody: {}", getJson(hmacBody));

        client.post().uri("/jwts/hmac/"+PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name()+"/secretkey")
                .bodyValue(data)
                .exchange().expectStatus().isOk().expectBody(Map.class).consumeWith(mapEntityExchangeResult -> {
                    LOG.info("result: {}", mapEntityExchangeResult.getResponseBody());
                        });

//        LOG.info("result: {}", result.getResponseBody().get("hmac"));
  //      assertThat(result.getResponseBody().get("hmac")).isNotNull();

    }

    public static String getJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        }
        catch (Exception e) {
            LOG.error("Failed to get json", e);
            return null;
        }
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


    private org.springframework.security.oauth2.jwt.Jwt jwt(String subjectName) {
        return new org.springframework.security.oauth2.jwt.Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName));
    }

    private Consumer<HttpHeaders> addJwt(org.springframework.security.oauth2.jwt.Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
    }

}
