package co.com.pragma.r2dbc;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.LoanApplicationReport;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public class LoanApplicationAdapter extends ReactiveAdapterOperations<
        LoanApplication/* change for domain model */,
        LoanApplicationEntity/* change for adapter model */,
        Integer,
        LoanApplicationReactiveRepository
>  implements LoanApplicationRepository {

    public LoanApplicationAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, LoanApplication.class));
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        LoanApplicationEntity entity = LoanApplicationEntity.builder()
                .applicationId(loanApplication.getApplicationId()) // se genera en la BD
                .amount(loanApplication.getAmount())
                .termMonths(loanApplication.getTermMonths())
                .email(loanApplication.getEmail())
                .document(loanApplication.getDocument())
                .stateId(loanApplication.getStateId())
                .loanTypeId(loanApplication.getLoanTypeId())
                .build();

        return repository.save(entity)
                .map(saved -> LoanApplication.builder()
                        .applicationId(saved.getApplicationId())
                        .amount(saved.getAmount())
                        .termMonths(saved.getTermMonths())
                        .email(saved.getEmail())
                        .document(saved.getDocument())
                        .stateId(saved.getStateId())
                        .loanTypeId(saved.getLoanTypeId())
                        .build());
    }

    @Override
    public Flux<LoanApplication> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<LoanApplication> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<Void> deleteById(Integer id) {
        return repository.deleteById(id);
    }

    @Override
    public Flux<LoanApplicationReport> getLoanApplicationsReport(Integer id, String document, Integer term, String loanType, String state, Integer page, Integer pageSize) {
        return repository.findLoanApplicationsReport(id, document, term, loanType, state, page, pageSize)
                .map(entity -> co.com.pragma.model.loanapplication.LoanApplicationReport.builder()
                        .applicationId(entity.getApplicationId())
                        .document(entity.getDocument())
                        .email(entity.getEmail())
                        .amount(entity.getAmount())
                        .termMonths(entity.getTermMonths())
                        .loanType(entity.getLoanType())
                        .interestRate(entity.getInterestRate() != null ? entity.getInterestRate().toString() : null)
                        .loanState(entity.getLoanState())
                        .build());
    }
}
