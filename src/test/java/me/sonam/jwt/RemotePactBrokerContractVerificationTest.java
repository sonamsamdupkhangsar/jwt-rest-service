package me.sonam.jwt;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import au.com.dius.pact.provider.spring.junit5.WebFluxTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * This is disabled with the new jwt-validator library separated out to its on library.
 * This will verify the pact with the remote pact broker
 */
/*

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("jwt-rest-service")
@PactBroker(url="https://pactbroker.sonam.cloud")
*/

public class RemotePactBrokerContractVerificationTest {
/*
    @Autowired
    Router router;

    @Autowired
    Handler handler;

    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new WebFluxTarget(router.route(handler)));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }*/
}