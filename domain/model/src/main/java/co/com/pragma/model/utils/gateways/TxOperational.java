package co.com.pragma.model.utils.gateways;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface TxOperational {

    <T> Mono<T> execute(Supplier<Mono<T>> action);
}
