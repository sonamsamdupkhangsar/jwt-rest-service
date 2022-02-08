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

