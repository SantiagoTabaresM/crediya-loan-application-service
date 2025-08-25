package co.com.pragma.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(LoanApplicationRoute routeLoanApplication, HandlerV2 handlerV2) {
        return RouterFunctions
            .route()
            .path("/api/v1", builder -> builder.GET("/usecase/path", routeLoanApplication::listenGETUseCase).POST("/usecase/otherpath", routeLoanApplication::listenPOSTUseCase).GET("/otherusercase/path", routeLoanApplication::listenGETOtherUseCase))
            .path("/api/v2", builder -> builder.GET("/usecase/path", handlerV2::listenGETUseCase).POST("/usecase/otherpath", handlerV2::listenPOSTUseCase).GET("/otherusercase/path", handlerV2::listenGETOtherUseCase))
            .build();
        }
}
