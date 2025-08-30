package co.com.pragma.model.utils.gateways;

import reactor.core.publisher.Mono;

public interface SecurityUtilsPort {

    Mono<String> getUserId();

    Mono<String> getUserRole();

    Mono<String> getUserToken();

}
