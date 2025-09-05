package co.com.pragma.r2dbc;


import co.com.pragma.model.loanstate.LoanState;
import co.com.pragma.model.loanstate.gateways.LoanStateRepository;
import co.com.pragma.r2dbc.entity.LoanStateEntity;

import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;


@Repository
public class LoanStateAdapter extends ReactiveAdapterOperations<
        LoanState/* change for domain model */,
        LoanStateEntity/* change for adapter model */,
        Integer,
        LoanStateReactiveRepository
>  implements LoanStateRepository {
    public LoanStateAdapter(LoanStateReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, LoanState.class));
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }


}
