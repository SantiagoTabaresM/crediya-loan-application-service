package co.com.pragma.api;

import co.com.pragma.model.loanapplication.gateways.LoanApplicationWebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class AuthenticationWebClientAdapter implements LoanApplicationWebClient {

    private static final String USERS_EXISTS = "/api/v1/users/exists/{document}/{email}";
    private final WebClient webClient;

    public AuthenticationWebClientAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Boolean> checkUserExists(String document, String email, String jwt) {
        return webClient.get()
                .uri(USERS_EXISTS, document, email ) // endpoint del microservicio de autenticación
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .retrieve()
                .bodyToMono(Boolean.class);
    }



}