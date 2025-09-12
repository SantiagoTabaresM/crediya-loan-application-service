package co.com.pragma.model.loanapplication.gateways;

import reactor.core.publisher.Mono;

public interface SQSsender {

    Mono<String> sendNotification(String message);

    Mono<String> automaticReview(String message);

    Mono<String> incrementApprovedLoansReport(String message);
}
