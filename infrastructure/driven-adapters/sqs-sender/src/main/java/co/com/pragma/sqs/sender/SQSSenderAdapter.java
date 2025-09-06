package co.com.pragma.sqs.sender;

import co.com.pragma.model.loanapplication.gateways.SQSsender;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSenderAdapter implements SQSsender {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;


    @Override
    public Mono<String> sendNotification(String message) {
        return sendMessage(message, properties.notificationQueueUrl());
    }


    @Override
    public Mono<String> sendDebtCapacity(String message) {
        return sendMessage(message, properties.debtCapacityQueueUrl());
    }




    private Mono<String> sendMessage(String message, String queueUrl) {
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent to {} with id {}", queueUrl, response.messageId()))
                .map(SendMessageResponse::messageId);
    }


    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }
}
