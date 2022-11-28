package me.sonam.jwt;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

/**
 * Set Email route
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Swagger for Jwt service", version = "1.0", description = "Documentation APIs v1.0"))
public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/create/{username}/{audience}/{expireField}/{expireIn}"
                            , produces = {
                            MediaType.APPLICATION_JSON_VALUE}, method= RequestMethod.GET,
                            operation = @Operation(operationId="createJwt", responses = {

                                    @ApiResponse(responseCode = "200", description = "successful operation"),
                                    @ApiResponse(responseCode = "400", description = "invalid user id")},
                                    parameters = {@Parameter(in = ParameterIn.PATH, name="username"),
                                            @Parameter(in = ParameterIn.PATH, name="audience"),
                                            @Parameter(in = ParameterIn.PATH, name="expireField"),
                                            @Parameter(in = ParameterIn.PATH, name="expireIn")
                                    }
                            ))
            }
    )
    public RouterFunction<ServerResponse> route(Handler handler) {
        LOG.info("building router function");

        return RouterFunctions.route(GET("/create/{clientId}/{groupNames}/{username}/{audience}/{expireField}/{expireIn}").
                        and(accept(MediaType.APPLICATION_JSON)),
                handler::createJwt);
    }
}
