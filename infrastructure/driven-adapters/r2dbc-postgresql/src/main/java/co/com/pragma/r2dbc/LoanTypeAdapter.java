package co.com.pragma.r2dbc;



import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.r2dbc.entity.LoanTypeEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public class LoanTypeAdapter extends ReactiveAdapterOperations<
        LoanType/* change for domain model */,
        LoanTypeEntity/* change for adapter model */,
        Integer,
        LoanTypeReactiveRepository
>  implements LoanTypeRepository {
    public LoanTypeAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, LoanType.class));
    }

    @Override
    public Mono<LoanType> save(LoanType loanType) {
       return super.save(loanType);
    }

    @Override
    public Flux<LoanType> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<LoanType> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }


}
