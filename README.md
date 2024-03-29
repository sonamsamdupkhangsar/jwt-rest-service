# jwt-rest-service
This is jwt-rest-service for issuing Jwt tokens

This service create a RSA public/private keypair on startup.  The private jwtKey will be stored in a database.
The public jwtKey will be shared with internal service thru using the [jwt-validator](https://github.com/sonamsamdupkhangsar/jwt-validator) library for validating the jwt token.
 
## Consumer
This service is consumed by the [authentication-rest-service](https://github.com/sonamsamdupkhangsar/authentication-rest-service) during `authenticate` api call.

## Builing locally
Use the settings.xml file to build locally or on github.  It contains reference to pull down `jwt-issuer` library from github maven repository.
Use `mvn -s settings.xml -U test` to build locally.

## Run locally using profile
Use the following to run local profile which will pick up properties defined in the `application-local.yml` :

```
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```
or use the following to override:

```
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --jwt.issuer=sonam.us \
    --POSTGRES_USERNAME=test \
    --POSTGRES_PASSWORD=test \
    --POSTGRES_DBNAME=jwt \
    --POSTGRES_SERVICE=localhost:5432
    --DB_SSLMODE=disable
    --eureka.client.enabled=false"
```
 
## Build Docker image
Build docker image using included Dockerfile.
`docker build -t imageregistry/jwt-rest-service:1.0 .` 

## Push Docker image to repository
`docker push imageregistry/jwt-rest-service:1.0`

## Deploy Docker image locally
`docker run -e jwt.issuer=sonam.us
 -e POSTGRES_USERNAME=test \
 -e POSTGRES_PASSWORD=test -e POSTGRES_DBNAME=jwt \
 -e POSTGRES_SERVICE=localhost:5432
 --publish 8081:8080 imageregistry/jwt-rest-service:1.0`

Test jwt api using using swagger-ui at http://localhost:8080/swagger-ui.html

## Installation on Kubernetes
Use a Helm chart such as my one here @ [sonam-helm-chart](https://github.com/sonamsamdupkhangsar/sonam-helm-chart):

```helm install jwtapi sonam/mychart -f values.yaml --version 0.1.21 --namespace=backend```

For Helm dry run:
```
helm upgrade --install --timeout 5m0s \
            --set "image.repository=registry/jwt-rest-service" \
            --set "image.tag=1" \
            --set "project=jwt-rest-service" \                      
             jwt-rest-service \
            sonam/mychart -f values-backend.yaml --version 0.1.21 --namespace=backend --dry-run
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

## Secrets ecnryped with SealedSecret
This project uses SealedSecret to deploy a secret file that is needed by this project.
The secret-file is in `sealedsecret` folder.  This needs to deploy on a Kubernetes cluster.
For local development, use the application local profile.

SealedSecret file:
1. `jwt-rest-service-sealed.yaml` for hmac key initialization.
 
