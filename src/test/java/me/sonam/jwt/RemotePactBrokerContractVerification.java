package me.sonam.jwt;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * For this to work the {@link JwtRestServiceRun} class must be running
 * since it will assume the provider service is running locally on a RANDOM_PORT
 *  This localhost config is defined in the maven plugin section.
 *  Run this locally and then integrate with CI as separate mvn pact:verify step
 *  and indicate where the provider is running in the maven pom.
 *  Note: This will run when you run thru IDE as test case if the {@link JwtRestServiceRun} class is running
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("jwt-rest-service")
@PactBroker(url="https://pactbroker.sonam.cloud")
public class RemotePactBrokerContractVerification {

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}