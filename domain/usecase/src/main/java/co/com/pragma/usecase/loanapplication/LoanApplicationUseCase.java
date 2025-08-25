package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanApplicationUseCase implements ILoanApplicationUseCase  {

    private final LoanApplicationRepository loanApplicationRepository;

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return loanApplicationRepository.save(loanApplication);
    }

    public Mono<LoanApplication> updateLoanApplication(LoanApplication loanApplication) {
        return loanApplicationRepository.save(loanApplication);
    }

    public Flux<LoanApplication> getAllLoanApplications() {
        return loanApplicationRepository.findAll();
    }

    public Mono<LoanApplication> getLoanApplicationById(Integer id) {
        return loanApplicationRepository.findById(id);
    }

    public Mono<Void> deleteLoanApplication(Integer id) {
        return loanApplicationRepository.deleteById(id);
    }
    
}
