package me.sonam.jwt;

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
        final int expireInField = Calendar.DAY_OF_MONTH;
        final int expireIn = 10;

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
}
