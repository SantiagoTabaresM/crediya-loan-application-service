package co.com.pragma.model.loanapplication.gateways;

import co.com.pragma.model.loanapplication.UserInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationWebClient {

    Mono<Boolean> checkUserExists(String document, String email, String jwt);

    Flux<UserInfo> getUsersByDocuments(String[] documents, String jwt);

}
