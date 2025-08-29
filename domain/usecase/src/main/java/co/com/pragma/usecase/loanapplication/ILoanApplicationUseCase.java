package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ILoanApplicationUseCase {

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication);

    public Mono<LoanApplication> updateLoanApplication(LoanApplication loanApplication);

    public Flux<LoanApplication> getAllLoanApplications();

    public Mono<LoanApplication> getLoanApplicationById(Integer id);

    public Mono<Void> deleteLoanApplication(Integer id) ;
    
}
