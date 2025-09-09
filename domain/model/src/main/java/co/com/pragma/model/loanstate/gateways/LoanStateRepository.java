package co.com.pragma.model.loanstate.gateways;

import reactor.core.publisher.Mono;

public interface LoanStateRepository {

    Mono<Boolean> existsById(Integer id);
}
