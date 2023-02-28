package me.sonam.jwt;

import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.JwtException;
import me.sonam.security.jwt.PublicKeyJwtCreator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(JwtServiceTest.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PublicKeyJwtCreator jwtCreator;

    @Value("${jwt.issuer}")
    private String issuer;

    // get a jwt and validate the jwt validation is true or is is valid
    @Test
    public void createJwtAndValidate() {
        final String clientId = "sonam-123-322";
        final String subject = UUID.randomUUID().toString();
        LOG.info("uuid: {}", UUID.randomUUID());
        final String audience = "email"; //the resource to access
        final String scopes = "email.write";
        final String role = "user";
        final String groups = "email, messaging";
        final String secretKey = "mysecret";

        jwtCreator.generateKey(clientId, secretKey).subscribe(hmacKey1 -> LOG.info("crate a HmacKey: {}", hmacKey1));

        JwtBody jwtBody = new JwtBody(subject, scopes, clientId, audience, role, groups, 10, null);
        final String hmac = PublicKeyJwtCreator.getHmac(PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name(), PublicKeyJwtCreator.getJson(jwtBody), secretKey);

        //jwt token validate for 10 days
        Mono<String> stringMono = jwtService.create(Mono.just(jwtBody));

        stringMono.subscribe(s -> LOG.info("reponsee: {}", s));

        stringMono = jwtService.create(Mono.just(jwtBody));
        stringMono.as(StepVerifier::create).assertNext(jwt -> {
            assertThat(jwt).isNotNull();
            LOG.info("jwt is not null: {}", jwt);

        }).verifyComplete();

        LOG.info("try another create with an invalid hmac");

       // stringMono = jwtService.create(Mono.just(jwtBody));
       // stringMono.as(StepVerifier::create).expectError(JwtException.class).verify();
    }

    @Test
    public void createUUID() {
        LOG.info("uuid: {}", UUID.randomUUID().toString());
    }
}
