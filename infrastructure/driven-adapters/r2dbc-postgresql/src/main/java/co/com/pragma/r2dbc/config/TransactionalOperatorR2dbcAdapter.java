package co.com.pragma.r2dbc.config;

import co.com.pragma.model.utils.gateways.TxOperational;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
public class TransactionalOperatorR2dbcAdapter implements TxOperational {

    private final TransactionalOperator transactionalOperator;

    public TransactionalOperatorR2dbcAdapter(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
    }


    @Override
    public <T> Mono<T> execute(Supplier<Mono<T>> action) {
        // Delegamos la ejecución al TransactionalOperator de Spring
        return action.get().as(transactionalOperator::transactional);
    }
}


