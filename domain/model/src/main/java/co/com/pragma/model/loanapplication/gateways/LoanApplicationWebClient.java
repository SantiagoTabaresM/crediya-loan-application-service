package co.com.pragma.model.loanapplication.gateways;

import reactor.core.publisher.Mono;

public interface LoanApplicationWebClient {

    Mono<Boolean> checkUserExists(String document, String email, String jwt);

}
