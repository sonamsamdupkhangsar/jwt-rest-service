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

/**
 * this is not meant to be run during testing.
 * It is just for verifying a provider service to be running for verification
 * against pact consumers.
 */
//@EnableAutoConfiguration
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class JwtRestServiceRun {
    private static final Logger LOG = LoggerFactory.getLogger(JwtRestServiceRun.class);

  //  @Autowired
    private WebTestClient client;
/*
    @Value("${jwt.issuer}")
    private String issuer;

    @Test
    public void getJwt() throws InterruptedException {
        LOG.info("running sleep");

       *//* Thread.sleep(1000000L);
        LOG.info("done sleeping");*//*
    }*/
}
