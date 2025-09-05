package co.com.pragma.model.loanapplication.gateways;

import reactor.core.publisher.Mono;

public interface SQSsender {

    Mono<String> send(String message);
}
