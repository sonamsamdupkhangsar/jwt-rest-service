package me.sonam.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
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

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(JwtServiceTest.class);

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.issuer}")
    private String issuer;

    // get a jwt and validate the jwt validation is true or is is valid
    @Test
    public void createJwtAndValidate() {
        final String audience = "sonam.cloud";
        final String subject = "sonam";
        final int expireInField = Calendar.SECOND;
        final int expireIn = 3;

        //jwt token validate for 10 days
        Mono<String> stringMono = jwtService.create(subject, audience, expireInField, expireIn);

        stringMono.as(StepVerifier::create).assertNext(jwt-> {
            assertThat(jwt).isNotNull();
            LOG.info("jwt is not null: {}", jwt);

            jwtService.validate(jwt).as(StepVerifier::create).assertNext(map -> {


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
        }).verifyComplete();

    }

    @Test
    public void badSignature() {
        final String jwt= "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJz25hbSIsImlzcyI6InNvbmFtLmNsb3VkIiwiYXVkIjoic29uYW0uY2xvdWQiLCJqdGkiOiJmMTY2NjM1OS05YTViLTQ3NzMtOWUyNy00OGU0OTFlNDYzNGIifQ.KGFBUjghvcmNGDH0eM17S9pWkoLwbvDaDBGAx2AyB41yZ_8-WewTriR08JdjLskw1dsRYpMh9idxQ4BS6xmOCQ";
        try {
            jwtService.validate(jwt).as(StepVerifier::create).assertNext(map -> {
                fail("should not get here as jwt is a invalid signature by tampering");
                LOG.info("validate ?");

                LOG.info("verfied claims");
            }).verifyComplete();
        } catch (SignatureException signatureException) {
            LOG.error("signature error");
        }
    }

    @Test
    public void expiredJwt() {
        final String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzb25hbSIsImlzcyI6InNvbmFtLmNsb3VkIiwiYXVkIjoic29uYW0uY2xvdWQiLCJleHAiOjE2NTY0NTQ2NDQsImp0aSI6ImJmNjI4OTI1LTIzN2EtNDRlNy1hYjlmLTAwYTVjZmRmYzVkNSJ9.BafIk8NcNuR7YhJNe1BabDctzutlWkPM47EW3umCEaEXhrcXoKsT__daVpFkVru2Y-oXFbRwv7I4hJxlXWZK1A";
        try {
            jwtService.validate(jwt).as(StepVerifier::create).assertNext(map -> {
                fail("jwt is expired");
            }).verifyComplete();
        }
        catch (ExpiredJwtException expiredJwtException) {
            LOG.error("jwt is expired as expected");
        }
    }
}
