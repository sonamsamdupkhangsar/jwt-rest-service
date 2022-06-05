# jwt-rest-service

This is JWT (Json Web Token) Rest Service for issuing Jwt tokens and validating them. 
This is a reactive Java webservice api.


## Run locally

`mvn spring-boot:run -Dspring-boot.run.arguments="--jwt.issuer=dummy.com \
 --jwt.secret=supersecret"`
 
 
## Build Docker image

Build docker image using included Dockerfile.


`docker build -t imageregistry/jwt-rest-service:1.0 .` 

## Push Docker image to repository

`docker push imageregistry/jwt-rest-service:1.0`

## Deploy Docker image locally

`docker run -e jwt.issuer=dummy.com -e jwt.secret=supersecret
 --publish 8080:8080 imageregistry/jwt-rest-service:1.0`

Test jwt api using using swagger-ui at http://localhost:8080/swagger-ui.html

## Installation on Kubernetes
Use a Helm chart such as my one here @ [sonam-helm-chart](https://github.com/sonamsamdupkhangsar/sonam-helm-chart):

```helm install jwtapi sonam/mychart -f values.yaml --version 0.1.12 --namespace=backend```

For dry run:
```
helm upgrade --install --timeout 5m0s \
            --set "image.repository=registry/jwt-rest-service" \
            --set "image.tag=1" \
            --set "project=jwt-rest-service" \
            --set "envs[0].name=JWT_SECRET" --set "envs[0].value=hello" \
            --set "envs[1].name=JWT_ISSUER" --set "envs[1].value=world" \
             jwt-rest-service \
            sonam/mychart -f values-backend.yaml --version 0.1.13 --namespace=backend --dry-run
```

### Verify Pacts with Pactbroker
To verify pact with the remote broker the provider service must be running.  The provider
service host is defined in the `serviceProvider` section of maven pom.

To verify Provider with consumer pact from a pact broker run `mvn pact:verify`.
This will need to be done on its own after the code has been deployed to test this
provider with consumer pacts.  You will need to supply
`${pactbrokerurl}`, `${pactbrokerusername}`, `${pactbrokerpassword}` to update results
to the pact broker.
Using the current deplooy.yaml workflow, this will be done after we do a helm upgrade or install. 


To Push Pact verification results run two separate terminals.
On one run `mvn test -Dtest=me.sonam.jwt.JwtRestServiceRun`
and on another ` mvn pact:verify -Dpactbrokerurl= -Dpactbrokerusername= -Dpactbrokerpassword=`

This will publish the provider verification results to pactbroker.

