package co.com.pragma.model.loanapplication.gateways;

import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// PUERTOS SECUNDARIOS
public interface LoanApplicationRepository {

    Mono<LoanApplication> save(LoanApplication loanApplication);

    Flux<LoanApplication> findAll();

    Mono<LoanApplication> findById(Integer id);

    Mono<Void> deleteById(Integer id);
}
